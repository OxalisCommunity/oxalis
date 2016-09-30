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

package eu.peppol.document;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;

/**
 * Converts a generic SBDH to the corresponding, much simpler PEPPOL sbdh header.
 *
 * @author steinar
 */
public class Sbdh2PeppolHeaderConverter {

    public static PeppolStandardBusinessHeader convertSbdh2PeppolHeader(StandardBusinessDocumentHeader standardBusinessDocumentHeader) {
        PeppolStandardBusinessHeader peppolSbdh = new PeppolStandardBusinessHeader();

        // Skipping Header version and manifest (not used right now)

        // Receiver
        String receiver = getReceiver(standardBusinessDocumentHeader);
        peppolSbdh.setRecipientId(new ParticipantId(receiver));

        // Sender
        String sender = getSender(standardBusinessDocumentHeader);
        peppolSbdh.setSenderId(new ParticipantId(sender));

        // Message id
        String messageId = getMessageId(standardBusinessDocumentHeader);
        peppolSbdh.setMessageId(new MessageId(messageId));

        // Computes the document type and process/profile type identifier
        parseDocumentIdentificationAndProfileIdentification(peppolSbdh, standardBusinessDocumentHeader);

        // Date / time conversion
        XMLGregorianCalendar creationDateAndTime = standardBusinessDocumentHeader.getDocumentIdentification().getCreationDateAndTime();
        Calendar cal = creationDateAndTime.toGregorianCalendar();
        peppolSbdh.setCreationDateAndTime(cal.getTime());

        return peppolSbdh;
    }

    static void parseDocumentIdentificationAndProfileIdentification(PeppolStandardBusinessHeader peppolMessageMetaData, StandardBusinessDocumentHeader standardBusinessDocumentHeader) {
        for (Scope scope : standardBusinessDocumentHeader.getBusinessScope().getScope()) {
            if (scope.getType().equalsIgnoreCase("DOCUMENTID")) {
                String documentIdentifier = scope.getInstanceIdentifier();
                peppolMessageMetaData.setDocumentTypeIdentifier(PeppolDocumentTypeId.valueOf(documentIdentifier));
                continue;
            }
            if (scope.getType().equalsIgnoreCase("PROCESSID")) {
                String processTypeIdentifer = scope.getInstanceIdentifier();
                peppolMessageMetaData.setProfileTypeIdentifier(new PeppolProcessTypeId(processTypeIdentifer));
                continue;
            }
        }
    }

    static String getSender(StandardBusinessDocumentHeader standardBusinessDocumentHeader) {
        Partner partner = standardBusinessDocumentHeader.getSender().get(0);
        return partner.getIdentifier().getValue();
    }

    static String getMessageId(StandardBusinessDocumentHeader standardBusinessDocumentHeader) {
        DocumentIdentification documentIdentification = standardBusinessDocumentHeader.getDocumentIdentification();
        return documentIdentification.getInstanceIdentifier();
    }

    static String getReceiver(StandardBusinessDocumentHeader standardBusinessDocumentHeader) {
        Partner partner = standardBusinessDocumentHeader.getReceiver().get(0);
        PartnerIdentification identifier = partner.getIdentifier();
        return identifier.getValue();
    }
}