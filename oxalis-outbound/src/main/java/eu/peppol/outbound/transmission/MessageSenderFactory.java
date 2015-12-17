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

package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.smp.SmpLookupManager;

/**
 * Builds a MessageSender, which is suitable for sending messages of the given type.
 *
 * This implementation assumes that all the required meta data is supplied, i.e. no attempt is made reading the file
 * in order to determine the input values.
 *
 *
 * The typical input to the sender process consists of the following meta data or header items :
 * <ul>
 *     <li>The PEPPOL identifier of the receiver, which can be deducted from the message to be sent.
 *          I.e. the content of <code>/Invoice/cac:AccountingSupplierParty/cac:Party/cbc:EndpointID</code>
 *          </li>
 *     <li>The PEPPOL identifier of the sender. I.e. the contents of <code>/Invoice/cac:AccountingCustomerParty/cac:Party/cbc:EndpointID</code>
 *     </li>
 *     <li>The PEPPOL document type identifier, which could possibly be determined from the contents of the message using these elements:
 *          <ol>
 *              <li>The root name space, for instance <code>urn:oasis:names:specification:ubl:schema:xsd:Invoice-2</code> found in the default XML name space</li>
 *              <li>The local name of the root element, i.e. &lt;Invoice&gt;</li>
 *              <li>The customization found in <code>/Invoice/cbc:CustomizationID</code></li>
 *          </ol>
 *     </li>
 *     <li>The actual document/message to be sent, which can have two main layouts:</li>
 *     <ol>
 *         <li>PEPPOL Document Type instantiated as an XML document wrapped in a SBDH XML envelope.</li>
 *         <li>PEPPOL Document Type instantiated as an XML document <em>without</em> being wrapped in an SBDH XML enevelope.</li>
 *     </ol>
 *     <li>The PEPPOL Process identifier, which is not really used for anything just now. This value is best obtained from the SMP</li>
 * </ul>
 *
 * <p>Caveat! The two EndpointID elements are not mandatory as per the UBL schema. They are however strongly recommended in the Norwegian EHF-format.
 * Henceforth; determining the above identifiers can be somewhat risky.
 * </p>
 *
 * @author steinar
 *
 *         Date: 29.10.13
 *         Time: 18:20
 */
class MessageSenderFactory {

    SmpLookupManager smpLookupManager;
    private final As2MessageSender as2MessageSender;

    @Inject
    MessageSenderFactory(SmpLookupManager smpLookupManager, As2MessageSender as2MessageSender) {
        this.smpLookupManager = smpLookupManager;
        this.as2MessageSender = as2MessageSender;
    }

    MessageSender createMessageSender(ParticipantId receiver, PeppolDocumentTypeId peppolDocumentTypeId) {
        SmpLookupManager.PeppolEndpointData peppolEndpointData = getBusDoxProtocolFor(receiver, peppolDocumentTypeId);

        return createMessageSender(peppolEndpointData.getBusDoxProtocol());
    }

    SmpLookupManager.PeppolEndpointData getBusDoxProtocolFor(ParticipantId participantId, PeppolDocumentTypeId documentTypeIdentifier) {
        SmpLookupManager.PeppolEndpointData endpointData = smpLookupManager.getEndpointTransmissionData(participantId, documentTypeIdentifier);

        return endpointData;
    }

    MessageSender createMessageSender(BusDoxProtocol busDoxProtocol) {
        switch (busDoxProtocol) {
            case AS2:
                return as2MessageSender;
            default:
                throw new IllegalStateException("Invalid or unknown protocol: " + busDoxProtocol);
        }
    }

}
