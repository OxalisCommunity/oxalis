package eu.peppol.as2;

/**
 * @author steinar
 *         Date: 22.10.13
 *         Time: 15:55
 */
public class Mic {
    private final String digestAsString;
    private final String algorithmName;

    public Mic(String digestAsString, String algorithmName) {
        this.digestAsString = digestAsString;
        this.algorithmName = algorithmName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(digestAsString).append(", ").append(algorithmName);

        return sb.toString();
    }

    public static Mic valueOf(String receivedContentMic) {
        String s[] = receivedContentMic.split(",");
        if (s.length != 2) {
            throw new IllegalArgumentException("Invalid mic: '" + receivedContentMic + "'. Required syntax: encoded-message-digest \",\" (sha1|md5)");
        }
        return new Mic(s[0].trim(), s[1].trim());
    }
}
