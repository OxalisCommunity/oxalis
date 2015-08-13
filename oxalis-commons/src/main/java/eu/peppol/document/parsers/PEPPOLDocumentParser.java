package eu.peppol.document.parsers;

import eu.peppol.identifier.ParticipantId;

/**
 * A small set of common information we should be able to retrieve from any PEPPOL UBL/EHF document.
 * TODO decide if getSenderReference getReceiverReference should be implemented (bonus)
 *
 * @author thore
 */
public interface PEPPOLDocumentParser {

    /**
     * Identify and return the PEPPOL participant sending the document.
     */
    public ParticipantId getSender();

    /**
     * Identify and return the PEPPOL participant receiving the document.
     */
    public ParticipantId getReceiver();

    /**
     * Identify and return references that connects the document to the sender side.
     * Could be invoice-number, order-number etc (should be in prioritized order)
    public List<DocumentReference> getSenderReference();
     */

    /**
     * Identify and return references that connects the document to the receiver side.
     * Could be your-ref, ordered-by etc (should be in prioritized order)
    public List<DocumentReference> getReceiverReference();
     */

}
