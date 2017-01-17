package eu.peppol.as2.outbound;

class SendResult {

    public final byte[] signedMimeMdnBytes;

    public SendResult(byte[] signedMimeMdnBytes) {
        this.signedMimeMdnBytes = signedMimeMdnBytes;
    }
}
