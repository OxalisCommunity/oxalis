/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.sniffer.document;

import lombok.extern.slf4j.Slf4j;
import network.oxalis.sniffer.document.parsers.*;
import network.oxalis.sniffer.identifier.CustomizationIdentifier;
import network.oxalis.sniffer.identifier.PeppolDocumentTypeId;
import network.oxalis.vefa.peppol.common.model.ProcessIdentifier;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;

/**
 * Parses the common PEPPOL header information, enough to decide document type and profile
 *
 * @author steinar
 * @author thore
 * @author arun
 */
@Slf4j
public class PlainUBLHeaderParser extends PlainUBLParser {

    public PlainUBLHeaderParser(Document document, XPath xPath) {
        super(document, xPath);
    }

    public CustomizationIdentifier fetchCustomizationId() {
        String value = retriveValueForXpath("//cbc:CustomizationID");
        return CustomizationIdentifier.valueOf(value);
    }

    public ProcessIdentifier fetchProcessTypeId() {
        String value = retriveValueForXpath("//cbc:ProfileID");
        return ProcessIdentifier.of(value);
    }

    public PeppolDocumentTypeId fetchDocumentTypeId() {
        CustomizationIdentifier customizationIdentifier = fetchCustomizationId();
        return new PeppolDocumentTypeId(rootNameSpace(), localName(), customizationIdentifier, ublVersion());
    }

    public PEPPOLDocumentParser createDocumentParser() {
        String type = localName();
        log.debug("Creating DocumentParser for type : " + localName());
        // despatch advice scenario
        if ("DespatchAdvice".equalsIgnoreCase(type)) return new DespatchAdviceDocumentParser(this);
        // catalogue scenario
        if ("Catalogue".equalsIgnoreCase(type)) return new CatalogueDocumentParser(this);
        // invoice scenario
        if ("CreditNote".equalsIgnoreCase(type)) return new InvoiceDocumentParser(this);
        if ("Invoice".equalsIgnoreCase(type)) return new InvoiceDocumentParser(this);
        if ("Reminder".equalsIgnoreCase(type)) return new InvoiceDocumentParser(this);
        // order scenario
        if ("Order".equalsIgnoreCase(type)) return new OrderDocumentParser(this);
        if ("OrderResponse".equalsIgnoreCase(type)) return new OrderDocumentParser(this);
        if ("OrderResponseSimple".equalsIgnoreCase(type)) return new OrderDocumentParser(this);
        // application response used by CatalogueResponse, MessageLevelResponse
        if ("ApplicationResponse".equalsIgnoreCase(type)) return new ApplicationResponseDocumentParser(this);
        // unknown scenario - for now we do not have a backup plan
        throw new IllegalStateException("Cannot decide which PEPPOLDocumentParser to use for type " + type);
    }

}
