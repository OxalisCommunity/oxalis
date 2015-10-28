package eu.peppol.sbdh;

import eu.peppol.identifier.*;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Scope;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

/**
 * Wrapper for SBDH.
 */
public class SbdhMessageHeader implements MessageHeader {

    private StandardBusinessDocumentHeader header;

    public SbdhMessageHeader(StandardBusinessDocumentHeader header) {
        this.header = header;
    }

    @Override
    public ParticipantId getFrom() {
        if (header.getSender().size() == 0)
            return null;
        return new ParticipantId(header.getSender().get(0).getIdentifier().getValue());
    }

    @Override
    public ParticipantId getTo() {
        if (header.getReceiver().size() == 0)
            return null;
        return new ParticipantId(header.getReceiver().get(0).getIdentifier().getValue());
    }

    @Override
    public PeppolDocumentTypeId getDocumentIdentifier() {
        return PeppolDocumentTypeId.valueOf(getScopeValue("DOCUMENTID"));
    }

    @Override
    public MessageId getInstanceIdentifier() {
        return new MessageId(header.getDocumentIdentification().getInstanceIdentifier());
    }

    @Override
    public PeppolProcessTypeId getProcessIdentifier() {
        return PeppolProcessTypeId.valueOf(getScopeValue("PROCESSID"));
    }

    private String getScopeValue(String scopeType) {
        for (Scope scope : header.getBusinessScope().getScope()) {
            if (scopeType.equals(scope.getType()))
                return scope.getInstanceIdentifier();
        }

        return null;
    }
}
