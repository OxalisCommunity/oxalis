/*
 * Copyright (c) 2015 Steinar Overbeck Cook
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
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
 * Converts an SBDH to the corresponding, much simpler PEPPOL header.
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