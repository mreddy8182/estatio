/*
 *
 *  Copyright 2012-2015 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.capex.dom.project;

import java.math.BigDecimal;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.schema.utils.jaxbadapters.PersistentEntityAdapter;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancyRepository;

import org.incode.module.base.dom.types.NameType;
import org.incode.module.base.dom.types.ReferenceType;
import org.incode.module.base.dom.utils.TitleBuilder;
import org.incode.module.base.dom.with.WithReferenceUnique;
import org.incode.module.docfragment.dom.types.AtPathType;

import org.estatio.dom.UdoDomainObject;
import org.estatio.dom.apptenancy.WithApplicationTenancyGlobalAndCountry;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.party.Party;
import org.estatio.dom.tax.Tax;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(
		identityType = IdentityType.DATASTORE
		,schema = "dbo"
)
@DatastoreIdentity(strategy = IdGeneratorStrategy.NATIVE, column = "id")
@Version(strategy = VersionStrategy.VERSION_NUMBER, column = "version")
@Unique(members={"reference"})
@Queries({
		@Query(name = "findByReference", language = "JDOQL", value = "SELECT "
				+ "FROM org.estatio.capex.dom.project.Project "
				+ "WHERE reference == :reference "),
		@Query(name = "matchByReferenceOrName", language = "JDOQL", value = "SELECT "
				+ "FROM org.estatio.capex.dom.project.Project "
				+ "WHERE reference.matches(:matcher) || name.matches(:matcher) ") })
@DomainObject(
		editing = Editing.DISABLED,
		objectType = "org.estatio.capex.dom.project.Project",
		autoCompleteRepository = ProjectRepository.class
)
@DomainObjectLayout(bookmarking = BookmarkPolicy.AS_ROOT)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
public class Project extends UdoDomainObject<Project> implements
		WithReferenceUnique, WithApplicationTenancyGlobalAndCountry {

	public Project() {
		super("reference, name, startDate");
	}
	
	public String title(){
		return TitleBuilder.start().withReference(getReference()).withName(getName()).toString();
	}

	@Column(length = ReferenceType.Meta.MAX_LEN, allowsNull = "false")
	@Property(regexPattern = ReferenceType.Meta.REGEX)
	@PropertyLayout(describedAs = "Unique reference code for this project")
	@Getter @Setter
	private String reference;

	@Column(length = NameType.Meta.MAX_LEN, allowsNull = "false")
    @Getter @Setter
    private String name;

	@Column(allowsNull = "true")
    @Getter @Setter
    private LocalDate startDate;

	@Column(allowsNull = "true")
	@Persistent
	@MemberOrder(sequence="4")
    @Getter @Setter
    private LocalDate endDate;

	@Column(allowsNull = "false", length = AtPathType.Meta.MAX_LEN)
	@Getter @Setter
	@Property(hidden = Where.EVERYWHERE)
	private String atPath;

	@PropertyLayout(
			named = "Application Level",
			describedAs = "Determines those users for whom this object is available to view and/or modify.",
			hidden = Where.PARENTED_TABLES
	)
	public ApplicationTenancy getApplicationTenancy() {
		return applicationTenancyRepository.findByPath(getAtPath());
	}

	@Persistent(mappedBy = "project", dependentElement = "true")
	@Getter @Setter
	private SortedSet<ProjectItem> items = new TreeSet<>();

	@Persistent(mappedBy = "parent", dependentElement = "true")
	@Getter @Setter
	private SortedSet<Project> children = new TreeSet<Project>();

	@Column(allowsNull = "true", name = "parentId")
	@Getter @Setter
	private Project parent;

	@MemberOrder(name="items", sequence = "1")
	public Project addItem(
			final Charge charge,
			final String description,
			@Parameter(optionality = Optionality.OPTIONAL)
			final BigDecimal budgetedAmount,
			@Parameter(optionality = Optionality.OPTIONAL)
			final LocalDate startDate,
			@Parameter(optionality = Optionality.OPTIONAL)
			final LocalDate endDate,
			final org.estatio.dom.asset.Property property,
			@Parameter(optionality = Optionality.OPTIONAL)
			final Tax tax
	) {
		projectItemRepository.findOrCreate(
				this, charge, description, budgetedAmount, startDate, endDate, property, tax);
		return this;
	}

	@Persistent(mappedBy = "project")
	@Getter @Setter
	private SortedSet<ProjectRole> roles = new TreeSet<>();

	@MemberOrder(name = "roles", sequence = "1")
	public Project newRole(
			final Party party,
			final ProjectRoleTypeEnum type,
			@Parameter(optionality = Optionality.OPTIONAL)
			final LocalDate startDate,
			@Parameter(optionality = Optionality.OPTIONAL)
			final LocalDate endDate){
		projectRoleRepository.create(this, party, type, startDate, endDate);
		return this;
	}

	@Inject
	ApplicationTenancyRepository applicationTenancyRepository;

	@Inject
	ProjectItemRepository projectItemRepository;

	@Inject
	private ProjectRoleRepository projectRoleRepository;

}
