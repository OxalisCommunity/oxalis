package eu.peppol.security;

import javax.security.auth.x500.X500Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of the Common Name (CN) of a X.509 certificate.
 *
 * @author steinar
 *         Date: 21.11.13
 *         Time: 21:55
 */
public class CommonName {

    public static final String CN_PATTERN = "CN=([^,]*),";
    public static final Pattern commonNamePattern = Pattern.compile(CN_PATTERN);

    private final String commonNameTextValue;

    public CommonName(X500Principal x500Principal) {
        String distinguishedName = x500Principal.getName();
        Matcher m = commonNamePattern.matcher(distinguishedName);
        if (m.find()) {
            commonNameTextValue = m.group(1);
        } else {
            throw new IllegalArgumentException("Unable to extract the CN attribute from " + x500Principal.getName());
        }
    }

    public CommonName(String commonNameTextValue) {
        this.commonNameTextValue = commonNameTextValue;
    }

    @Override
    public String toString() {
        return commonNameTextValue;
    }
}
