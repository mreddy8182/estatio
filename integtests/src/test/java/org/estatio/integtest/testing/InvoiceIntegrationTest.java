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
package org.estatio.integtest.testing;

import java.util.List;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import org.estatio.dom.charge.Charge;
import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.invoice.InvoiceStatus;
import org.estatio.dom.invoice.PaymentMethod;
import org.estatio.dom.lease.invoicing.InvoiceItemForLease;
import org.estatio.fixture.invoice.InvoiceFixture;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InvoiceIntegrationTest extends AbstractEstatioIntegrationTest {

    @Test
    public void t1_chargeCanBeFound() throws Exception {
        Charge charge = charges.findChargeByReference("RENT");
        Assert.assertEquals(charge.getReference(), "RENT");
    }

    @Test
    public void t2_invoiceCanBeFound() throws Exception {
        Invoice invoice = invoices.findMatchingInvoice(parties.findPartyByReferenceOrName(InvoiceFixture.SELLER_PARTY), parties.findPartyByReferenceOrName(InvoiceFixture.BUYER_PARTY), PaymentMethod.DIRECT_DEBIT, leases.findLeaseByReference(InvoiceFixture.LEASE), InvoiceStatus.NEW, InvoiceFixture.START_DATE);
        Assert.assertNotNull(invoice);
    }

    @Test
    public void t3_invoiceItemCanBeFound() throws Exception {
        List<InvoiceItemForLease> invoiceItems = invoiceItemsForLease.findInvoiceItemsByLease(InvoiceFixture.LEASE, InvoiceFixture.START_DATE, InvoiceFixture.START_DATE);
        Assert.assertThat(invoiceItems.size(), Is.is(2));
    }

    @Test
    public void t4_invoiceCanBeRemoved() throws Exception {
        Invoice invoice = invoices.findMatchingInvoices(parties.findPartyByReferenceOrName(InvoiceFixture.SELLER_PARTY), parties.findPartyByReferenceOrName(InvoiceFixture.BUYER_PARTY), PaymentMethod.DIRECT_DEBIT, leases.findLeaseByReference(InvoiceFixture.LEASE), InvoiceStatus.NEW, InvoiceFixture.START_DATE).get(0);
        invoice.remove();
        Assert.assertThat(invoices.findMatchingInvoices(parties.findPartyByReferenceOrName(InvoiceFixture.SELLER_PARTY), parties.findPartyByReferenceOrName(InvoiceFixture.BUYER_PARTY), PaymentMethod.DIRECT_DEBIT, leases.findLeaseByReference(InvoiceFixture.LEASE), InvoiceStatus.NEW, InvoiceFixture.START_DATE).size(), Is.is(0));
    }
}
