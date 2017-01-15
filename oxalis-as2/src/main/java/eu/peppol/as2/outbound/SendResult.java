package eu.peppol.as2.outbound;

import eu.peppol.identifier.MessageId;

class SendResult {
    final MessageId messageId;
    final byte[] signedMimeMdnBytes;

    public SendResult(MessageId messageId, byte[] signedMimeMdnBytes) {
        this.messageId = messageId;
        this.signedMimeMdnBytes = signedMimeMdnBytes;
    }
}
