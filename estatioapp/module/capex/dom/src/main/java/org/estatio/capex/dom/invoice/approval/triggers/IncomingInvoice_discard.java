package org.estatio.capex.dom.invoice.approval.triggers;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.incode.module.document.dom.impl.docs.Document;

import org.estatio.capex.dom.documents.LookupAttachedPdfService;
import org.estatio.capex.dom.documents.categorisation.IncomingDocumentCategorisationStateTransition;
import org.estatio.capex.dom.documents.categorisation.IncomingDocumentCategorisationStateTransitionType;
import org.estatio.capex.dom.invoice.IncomingInvoice;
import org.estatio.capex.dom.invoice.approval.IncomingInvoiceApprovalStateTransitionType;

@Mixin(method = "act")
public class IncomingInvoice_discard extends IncomingInvoice_triggerAbstract {

    private final IncomingInvoice incomingInvoice;

    public IncomingInvoice_discard(IncomingInvoice incomingInvoice) {
        super(incomingInvoice, IncomingInvoiceApprovalStateTransitionType.DISCARD);
        this.incomingInvoice = incomingInvoice;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(cssClassFa = "trash-o")
    public IncomingInvoice act(@Nullable final String comment) {
        Optional<Document> documentIfAny = lookupAttachedPdfService.lookupIncomingInvoicePdfFrom(incomingInvoice);
        documentIfAny.ifPresent(document ->
                stateTransitionService.trigger(
                        document,
                        IncomingDocumentCategorisationStateTransition.class,
                        IncomingDocumentCategorisationStateTransitionType.DISCARD_ASSOCIATED,
                        comment));
        trigger(comment);
        return getDomainObject();
    }

    public boolean hideAct() {
        return cannotTransition();
    }

    public String disableAct() {
        return reasonGuardNotSatisified();
    }

    @Inject
    LookupAttachedPdfService lookupAttachedPdfService;

}
