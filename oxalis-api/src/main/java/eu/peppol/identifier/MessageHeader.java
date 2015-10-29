package eu.peppol.identifier;

public interface MessageHeader {

    ParticipantId getFrom();

    ParticipantId getTo();

    PeppolDocumentTypeId getDocumentIdentifier();

    MessageId getInstanceIdentifier();

    PeppolProcessTypeId getProcessIdentifier();

}
