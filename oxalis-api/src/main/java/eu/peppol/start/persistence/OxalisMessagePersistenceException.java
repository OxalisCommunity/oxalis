package eu.peppol.start.persistence;

import eu.peppol.start.identifier.PeppolMessageHeader;
import org.w3c.dom.Document;

/**
 * @author steinar
 *         Date: 09.08.13
 *         Time: 14:10
 */
public class OxalisMessagePersistenceException extends Exception {

    private static final String MSG = "Unable to persist XML document for ";
    private final PeppolMessageHeader messageHeader;
    private final Document document;

    public OxalisMessagePersistenceException(String unknownReceipientMsg, PeppolMessageHeader peppolMessageHeader, Document document) {
        super(unknownReceipientMsg);

        messageHeader = peppolMessageHeader;
        this.document = document;
    }

    public OxalisMessagePersistenceException(PeppolMessageHeader messageHeader, Document document) {
        this(MSG + messageHeader, messageHeader, document);
    }

    public OxalisMessagePersistenceException(PeppolMessageHeader messageHeader, Document document, Throwable cause) {
        super(MSG + messageHeader, cause);

        this.messageHeader = messageHeader;
        this.document = document;
    }


    PeppolMessageHeader getMessageHeader() {
        return messageHeader;
    }

    Document getDocument() {
        return document;
    }
}
