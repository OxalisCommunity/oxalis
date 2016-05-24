/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.document.parsers;

import eu.peppol.document.PlainUBLParser;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.SchemeId;
import org.w3c.dom.Element;

/**
 * Abstract implementation based on the PlainUBLParser to retrieve information from PEPPOL documents.
 * Contains common functionality to be used as a base for decoding types.
 *
 * @author thore
 */
public abstract class AbstractDocumentParser implements PEPPOLDocumentParser {

    protected PlainUBLParser parser;

    public AbstractDocumentParser(PlainUBLParser parser) {
        this.parser = parser;
    }

    @Override
    public abstract ParticipantId getSender();

    @Override
    public abstract ParticipantId getReceiver();

    /**
     * Retrieves the ParticipantId which is retrieved using the supplied XPath.
     */
    protected ParticipantId participantId(String xPathExpr) {
        ParticipantId ret;

        // first we retrieve the correct participant element
        Element element;
        try {
            element = parser.retrieveElementForXpath(xPathExpr);
        } catch (Exception ex) {
            // DOM parser throws "java.lang.IllegalStateException: No element in XPath: ..." if no Element is found
            throw new IllegalStateException("No ParticipantId found at " + xPathExpr);
        }

        // get value and any schemeId given
        String companyId = element.getFirstChild().getNodeValue().trim();
        String schemeIdTextValue = element.getAttribute("schemeID").trim();

        // check if we already have a valid participant 9908:987654321
        if (ParticipantId.isValidParticipantIdentifierSyntax(companyId)) {
            if (schemeIdTextValue.length() == 0) {
                // we accept participants with icd prefix if schemeId is missing ...
                ret = new ParticipantId(companyId);
            } else {
                // ... or when given schemeId matches the icd code stat eg NO:VAT matches 9908 from 9908:987654321
                if (companyId.startsWith(SchemeId.parse(schemeIdTextValue).getIso6523Icd() + ":")) {
                    ret = new ParticipantId(companyId);
                } else {
                    throw new IllegalStateException("ParticipantId at " + xPathExpr + " is illegal, schemeId '" + schemeIdTextValue + "' and icd code prefix of " + companyId + " does not match");
                }
            }
        } else {
            // try to add the given icd prefix to the participant id
            companyId = SchemeId.parse(schemeIdTextValue).getIso6523Icd() + ":" + companyId;
            if (!ParticipantId.isValidParticipantIdentifierSyntax(companyId)) {
                throw new IllegalStateException("ParticipantId syntax at " + xPathExpr + " evaluates to " + companyId + " and is invalid");
            }
            ret = new ParticipantId(companyId);
        }
        return ret;
    }

}