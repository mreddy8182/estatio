/*
 *
 *  Copyright 2012-2013 Eurocommercial Properties NV
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
package org.estatio.dom.contracttests;

import com.google.common.collect.ImmutableMap;

import org.estatio.dom.WithNameComparable;
import org.estatio.dom.ComparableByNameContractTestAbstract_compareTo;
import org.estatio.dom.asset.FixedAsset;
import org.estatio.dom.asset.FixedAssetForTesting;
import org.estatio.dom.party.Party;
import org.estatio.dom.party.PartyForTesting;


/**
 * Automatically tests all domain objects implementing {@link WithNameComparable}.
 */
public class WithNameComparableContractTestAll_compareTo extends ComparableByNameContractTestAbstract_compareTo {

    public WithNameComparableContractTestAll_compareTo() {
        super(Constants.packagePrefix, noninstantiableSubstitutes());
    }

    static ImmutableMap<Class<?>, Class<?>> noninstantiableSubstitutes() {
        return ImmutableMap.<Class<?>,Class<?>>of(
                FixedAsset.class, FixedAssetForTesting.class,
                Party.class, PartyForTesting.class);
    }

}
