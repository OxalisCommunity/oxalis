package eu.peppol.persistence;

import eu.peppol.PeppolMessageMetaData;

/**
 * @author steinar
 *         Date: 09.08.13
 *         Time: 14:10
 */
public class OxalisMessagePersistenceException extends Exception {

    private static final String MSG = "Unable to persist XML document for ";
    private final PeppolMessageMetaData peppolMessageMetaData;

    public OxalisMessagePersistenceException(String unknownReceipientMsg, PeppolMessageMetaData peppolMessageHeader) {
        super(unknownReceipientMsg);

        peppolMessageMetaData = peppolMessageHeader;
    }

    public OxalisMessagePersistenceException(PeppolMessageMetaData peppolMessageMetaData) {
        this(MSG + peppolMessageMetaData, peppolMessageMetaData);
    }

    public OxalisMessagePersistenceException(PeppolMessageMetaData peppolMessageMetaData, Throwable cause) {
        super(MSG + peppolMessageMetaData, cause);

        this.peppolMessageMetaData = peppolMessageMetaData;
    }
}
