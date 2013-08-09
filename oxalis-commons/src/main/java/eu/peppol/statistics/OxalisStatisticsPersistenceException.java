package eu.peppol.statistics;

import eu.peppol.start.identifier.PeppolMessageHeader;
import org.w3c.dom.Document;

/**
 * @author steinar
 *         Date: 09.08.13
 *         Time: 14:05
 */
public class OxalisStatisticsPersistenceException extends Exception {
    private final PeppolMessageHeader messageHeader;

    public OxalisStatisticsPersistenceException(PeppolMessageHeader messageHeader, Throwable cause) {
        super("Unabel to persist statistics for message header " + messageHeader.toString(), cause);
        this.messageHeader = messageHeader;
    }


    public PeppolMessageHeader getMessageHeader() {
        return messageHeader;
    }
}
