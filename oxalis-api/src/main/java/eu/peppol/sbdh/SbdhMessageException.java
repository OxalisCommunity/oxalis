package eu.peppol.sbdh;

import eu.peppol.PeppolMessageMetaData;

/**
 * @author steinar
 *         Date: 24.10.13
 *         Time: 12:57
 */
public class SbdhMessageException extends Exception {

    private final PeppolMessageMetaData peppolMessageMetaData;

    public SbdhMessageException(PeppolMessageMetaData peppolMessageMetaData, String msg) {
        super(msg);
        this.peppolMessageMetaData = peppolMessageMetaData;
    }

    public SbdhMessageException(PeppolMessageMetaData peppolMessageMetaData,String s, Throwable throwable) {
        super(s, throwable);
        this.peppolMessageMetaData = peppolMessageMetaData;
    }
}
