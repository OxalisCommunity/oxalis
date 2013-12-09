package eu.peppol.statistics;

import eu.peppol.PeppolMessageMetaData;

/**
 * @author steinar
 *         Date: 09.08.13
 *         Time: 14:05
 */
public class OxalisStatisticsPersistenceException extends Exception {
    private final PeppolMessageMetaData peppolMessageMetaData;

    public OxalisStatisticsPersistenceException(PeppolMessageMetaData peppolMessageMetaData, Throwable cause) {
        super("Unabel to persist statistics for message header " + peppolMessageMetaData.toString(), cause);
        this.peppolMessageMetaData = peppolMessageMetaData;
    }


    public PeppolMessageMetaData getPeppolMessageMetaData() {
        return peppolMessageMetaData;
    }
}
