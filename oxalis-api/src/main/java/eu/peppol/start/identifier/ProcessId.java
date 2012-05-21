package eu.peppol.start.identifier;

/**
 * Represents the unique set of Process ID values allowed.
 *
 * According to Policy 16:
 * <em></em>PEPPOL processes are identified by the respective BII processes. The process identifier has to match the BII profile ID.</em>
 *
 * @author Steinar Overbeck Cook
 *
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 19:18
 *
 * @see "Tranport Policy for using Identifiers"
 * @deprecated Use the {@link DocumentTypeIdentifier} class instead.
 */
@Deprecated
public enum ProcessId {

    ORDER_ONLY("urn:www.cenbii.eu:profile:bii03:ver1.0"),
    INVOICE_ONLY("urn:www.cenbii.eu:profile:bii04:ver1.0"),
    PROCUREMENT("urn:www.cenbii.eu:profile:bii06:ver1.0");

    // See Policy 15 and policy 17
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
        throw new IllegalStateException("Unkown Process ID: " + identifier);
    }

    public String stringValue() {
        return profileId;
    }
    
    @Override
    public String toString() {
        return stringValue();
    }
}
