package eu.peppol.as2.outbound;

import eu.peppol.identifier.MessageId;

class SendResult {
    final MessageId messageId;
    final byte[] remEvidenceBytes;
    final byte[] signedMimeMdnBytes;

    public SendResult(MessageId messageId, byte[] remEvidenceBytes, byte[] signedMimeMdnBytes) {
        this.messageId = messageId;
        this.remEvidenceBytes = remEvidenceBytes;
        this.signedMimeMdnBytes = signedMimeMdnBytes;
    }
}
