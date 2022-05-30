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

package network.oxalis.sniffer.document.parsers;

import network.oxalis.sniffer.document.PlainUBLParser;
import network.oxalis.sniffer.identifier.ParticipantId;
import network.oxalis.sniffer.identifier.SchemeId;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
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

    /**
     * Retrieves the ParticipantId which is retrieved using the supplied XPath.
     */
    protected ParticipantIdentifier participantId(String xPathExpr) {
        ParticipantId ret;

        // first we retrieve the correct participant element
        Element element;
        try {
            element = parser.retrieveElementForXpath(xPathExpr);
        } catch (Exception ex) {
            // DOM parser throws "java.lang.IllegalStateException: No element in XPath: ..." if no Element is found
            throw new IllegalStateException(String.format("No ParticipantId found at '%s'.", xPathExpr));
        }

        // get value and any schemeId given
        String companyId = element.getFirstChild().getNodeValue().trim();
        String schemeIdTextValue = element.getAttribute("schemeID").trim();

        // check if we already have a valid participant 9908:987654321
        if (ParticipantId.isValidParticipantIdentifierPattern(companyId)) {
            if (schemeIdTextValue.length() == 0) {
                // we accept participants with icd prefix if schemeId is missing ...
                ret = new ParticipantId(companyId);
            } else {
                // ... or when given schemeId matches the icd code stat eg NO:VAT matches 9908 from 9908:987654321
                if (companyId.startsWith(SchemeId.parse(schemeIdTextValue).getCode() + ":")) {
                    ret = new ParticipantId(companyId);
                } else {
                    throw new IllegalStateException(String.format(
                            "ParticipantId at '%s' is illegal, schemeId '%s' and icd code prefix of '%s' does not match",
                            xPathExpr, schemeIdTextValue, companyId));
                }
            }
        } else {
            // try to add the given icd prefix to the participant id
            companyId = String.format("%s:%s", SchemeId.parse(schemeIdTextValue).getCode(), companyId);
            if (!ParticipantId.isValidParticipantIdentifierPattern(companyId)) {
                throw new IllegalStateException(String.format(
                        "ParticipantId syntax at '%s' evaluates to '%s' and is invalid", xPathExpr, companyId));
            }
            ret = new ParticipantId(companyId);
        }
        return ret.toVefa();
    }

}
