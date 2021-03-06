/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
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
package org.estatio.capex.dom.documents;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Blob;

import org.incode.module.document.dom.api.DocumentService;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentRepository;
import org.incode.module.document.dom.impl.paperclips.Paperclip;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;
import org.incode.module.document.dom.impl.types.DocumentType;

import org.estatio.dom.asset.FixedAsset;
import org.estatio.dom.asset.PropertyRepository;
import org.estatio.dom.invoice.DocumentTypeData;

@DomainService(
        nature = NatureOfService.DOMAIN,
        objectType = "org.estatio.capex.dom.documents.IncomingDocumentRepository"
)
public class IncomingDocumentRepository extends DocumentRepository {

    @Programmatic
    public List<Document> findIncomingDocuments() {
        return queryResultsCache.execute(
                this::doFindIncomingDocuments,
                IncomingDocumentRepository.class,
                "findIncomingDocuments"
        );
    }

    private List<Document> doFindIncomingDocuments() {
        final List<Document> documents = findWithNoPaperclips();
        return Lists.newArrayList(
                FluentIterable.from(documents)
                .filter(document -> DocumentTypeData.docTypeDataFor(document).isIncoming())
                .toList()
        );
    }

    @Programmatic
    public List<Document> findUnclassifiedIncomingOrders() {
        return queryResultsCache.execute(
                this::doFindUnclassifiedIncomingOrders,
                IncomingDocumentRepository.class,
                "findUnclassifiedIncomingOrders"
        );
    }

    private List<Document> doFindUnclassifiedIncomingOrders() {
        final List<Document> documents = findAttachedToExactlyOneFixedAssetOnly();
        return Lists.newArrayList(
                FluentIterable.from(documents)
                        .filter(document -> DocumentTypeData.docTypeDataFor(document)==DocumentTypeData.INCOMING_ORDER)
                        .toList()
        );
    }

    @Programmatic
    public List<Document> findUnclassifiedIncomingInvoices() {
        return queryResultsCache.execute(
                this::doFindUnclassifiedIncomingInvoices,
                IncomingDocumentRepository.class,
                "findUnclassifiedIncomingInvoices"
        );
    }

    private List<Document> doFindUnclassifiedIncomingInvoices() {
        final List<Document> documents = findAttachedToExactlyOneFixedAssetOnly();
        return Lists.newArrayList(
                FluentIterable.from(documents)
                        .filter(document -> DocumentTypeData.docTypeDataFor(document)==DocumentTypeData.INCOMING_INVOICE)
                        .toList()
        );
    }

    @Programmatic
    public List<Document> findAllIncomingDocumentsByName(final String name) {
        final List<Document> documents = findAllIncomingDocuments();
        return Lists.newArrayList(
                FluentIterable.from(documents)
                .filter(document -> document.getName().equals(name))
                .toList()
        );
    }

    @Programmatic
    public List<Document> matchAllIncomingDocumentsByName(final String searchPhrase) {
        final List<Document> documents = findAllIncomingDocuments();
        return Lists.newArrayList(
                FluentIterable.from(documents)
                        .filter(document -> document.getName().toLowerCase().contains(searchPhrase.toLowerCase()))
                        .toList()
        );
    }

    @Programmatic
    public List<Document> findAllIncomingDocuments() {
        final List<Document> documents = repositoryService.allInstances(Document.class);
        return Lists.newArrayList(
                FluentIterable.from(documents)
                        .filter(document -> DocumentTypeData.docTypeDataFor(document).getNature()== DocumentTypeData.Nature.INCOMING)
                        .toList()
        );
    }

    // TODO: tackle this (and the filtering on DocumentType?) at db level
    private List<Document> findAttachedToExactlyOneFixedAssetOnly() {
        List<Document> result = new ArrayList<>();
        for (FixedAsset asset : propertyRepository.allProperties()){
            for (Paperclip paperclip : paperclipRepository.findByAttachedTo(asset)){
                Document document = (Document) paperclip.getDocument();
                if (paperclipRepository.findByDocument(document).size()==1) {
                    result.add(document);
                }
            }
        }
        return result;
    }

    @Programmatic
    public Document upsertAndArchive(final DocumentType type, final String atPath, final String name, final Blob blob){
        Document document = null;
        final List<Document> incomingDocumentsWithSameName = findAllIncomingDocumentsByName(name);
        if (incomingDocumentsWithSameName.size()>0){
            document = incomingDocumentsWithSameName.get(0);
        }
        if (document!=null){
            String prefix = "arch-".concat(clockService.nowAsLocalDateTime().toString("yyyy-MM-dd-HH-mm-ss")).concat("-");
            String archivedName = prefix.concat(document.getName());
            Document archivedDocument = documentService.createForBlob(document.getType(), document.getAtPath(), archivedName, document.getBlob());
            // update blobbytes of document
            document.setBlobBytes(blob.getBytes());
            // attach document to archived document
            paperclipRepository.attach(document, "", archivedDocument);
        } else {
            document = documentService.createForBlob(type, atPath, name, blob);
        }
        return document;
    }

    @Inject
    QueryResultsCache queryResultsCache;

    @Inject
    PaperclipRepository paperclipRepository;

    @Inject
    RepositoryService repositoryService;

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    ClockService clockService;

    @Inject
    DocumentService documentService;

}
