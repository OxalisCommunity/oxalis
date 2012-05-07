package eu.peppol.start.identifier;

/**
 * @author Steinar Overbeck Cook
 *
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 19:18
 */
public enum ProcessId {


    ORDER_ONLY("urn:www.cenbii.eu:profile:bii03:ver1.0"),
    INVOICE_ONLY("urn:www.cenbii.eu:profile:bii04:ver1.0"),
    ;

    private static final String scheme = "cenbii-procid-ubl";

    private String profileId = null;


    private ProcessId(String profileId) {
        this.profileId = profileId;
    }

    public static String getScheme() {
        return scheme;
    }

    public String getProfileId() {
        return profileId;
    }


    /** Creates the corresponding ProcessId based upon the supplied Peppol Process identifier */
    public static ProcessId valueFor(String identifier) {
        ProcessId result = null;

        for (ProcessId processId : values()) {
            if (processId.profileId.equals(identifier)) {
                return processId;
            }
        }
        return result;
    }

    public String stringValue() {
        return profileId;
    }
    
    @Override
    public String toString() {
        return stringValue();
    }
}
