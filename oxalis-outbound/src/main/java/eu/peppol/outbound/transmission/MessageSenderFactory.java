package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.identifier.PeppolDocumentTypeId;

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
    private final StartMessageSender startMessageSender;

    @Inject
    MessageSenderFactory(SmpLookupManager smpLookupManager, As2MessageSender as2MessageSender, StartMessageSender startMessageSender) {
        this.smpLookupManager = smpLookupManager;
        this.as2MessageSender = as2MessageSender;
        this.startMessageSender = startMessageSender;
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
            case START:
                return startMessageSender;
            default:
                throw new IllegalStateException("Invalid or unknown protocol: " + busDoxProtocol);
        }
    }

}
