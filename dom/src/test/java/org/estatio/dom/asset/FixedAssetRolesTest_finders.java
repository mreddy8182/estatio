package org.estatio.dom.asset;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.query.Query;
import org.apache.isis.core.commons.matchers.IsisMatchers;

import org.estatio.dom.FinderInteraction;
import org.estatio.dom.FinderInteraction.FinderMethod;
import org.estatio.dom.party.Party;
import org.estatio.dom.party.PartyForTesting;

public class FixedAssetRolesTest_finders {

    private FinderInteraction finderInteraction;

    private FixedAsset asset;
    private Party party;
    private FixedAssetRoleType type;
    private LocalDate startDate;
    private LocalDate endDate;
    
    private FixedAssetRoles fixedAssetRoles;

    @Before
    public void setup() {
        
        asset = new FixedAssetForTesting();
        party = new PartyForTesting();
        type = FixedAssetRoleType.ASSET_MANAGER;
        
        startDate = new LocalDate(2013,1,4);
        endDate = new LocalDate(2013,2,5);
        
        fixedAssetRoles = new FixedAssetRoles() {

            @Override
            protected <T> T firstMatch(Query<T> query) {
                finderInteraction = new FinderInteraction(query, FinderMethod.FIRST_MATCH);
                return null;
            }
            @Override
            protected List<FixedAssetRole> allInstances() {
                finderInteraction = new FinderInteraction(null, FinderMethod.ALL_INSTANCES);
                return null;
            }
            @Override
            protected <T> List<T> allMatches(Query<T> query) {
                finderInteraction = new FinderInteraction(query, FinderMethod.ALL_MATCHES);
                return null;
            }
        };
    }

    
    @Test
    public void findRole() {

        fixedAssetRoles.findRole(asset, party, type);
        
        assertThat(finderInteraction.getFinderMethod(), is(FinderMethod.FIRST_MATCH));
        assertThat(finderInteraction.getResultType(), IsisMatchers.classEqualTo(FixedAssetRole.class));
        assertThat(finderInteraction.getQueryName(), is("findRoleByAssetAndPartyAndType"));
        assertThat(finderInteraction.getArgumentsByParameterName().get("asset"), is((Object)asset));
        assertThat(finderInteraction.getArgumentsByParameterName().get("party"), is((Object)party));
        assertThat(finderInteraction.getArgumentsByParameterName().get("type"), is((Object)type));
        assertThat(finderInteraction.getArgumentsByParameterName().size(), is(3));
    }
    
    @Test
    public void findRole2() {

        // TODO: need also to search by dates
        fixedAssetRoles.findRole(asset, party, type, startDate, endDate);
        
        assertThat(finderInteraction.getFinderMethod(), is(FinderMethod.FIRST_MATCH));
        assertThat(finderInteraction.getResultType(), IsisMatchers.classEqualTo(FixedAssetRole.class));
        assertThat(finderInteraction.getQueryName(), is("findRoleByAssetAndPartyAndType"));
        assertThat(finderInteraction.getArgumentsByParameterName().get("asset"), is((Object)asset));
        assertThat(finderInteraction.getArgumentsByParameterName().get("party"), is((Object)party));
        assertThat(finderInteraction.getArgumentsByParameterName().get("type"), is((Object)type));
        assertThat(finderInteraction.getArgumentsByParameterName().size(), is(3));
    }

}
