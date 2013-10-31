package eu.peppol.outbound.api;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;

/**
 * Holds an outbound PEPPOL message.
 *
 * @author steinar
 *         Date: 31.10.13
 *         Time: 13:52
 */
public class PeppolMessage {

    private PeppolMessage(Builder builder) {

    }

    public static class Builder {
        PeppolDocumentTypeId peppolDocumentTypeId;

    }
}
