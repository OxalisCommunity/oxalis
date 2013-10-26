package eu.peppol.sbdh;

import eu.peppol.PeppolMessageInformation;

/**
 * @author steinar
 *         Date: 24.10.13
 *         Time: 12:57
 */
public class SbdhMessageException extends Exception {

    private final PeppolMessageInformation peppolMessageInformation;

    public SbdhMessageException(PeppolMessageInformation peppolMessageInformation, String msg) {
        super(msg);
        this.peppolMessageInformation = peppolMessageInformation;
    }

    public SbdhMessageException(PeppolMessageInformation peppolMessageInformation,String s, Throwable throwable) {
        super(s, throwable);
        this.peppolMessageInformation = peppolMessageInformation;
    }
}
