package no.difi.oxalis.commons.security;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CertificateUtils {

    private static final Pattern PATTERN_CN = Pattern.compile("CN=([^,]*),");

    public static String extractCommonName(X509Certificate certificate) {
        X500Principal principal = certificate.getSubjectX500Principal();
        Matcher m = PATTERN_CN.matcher(principal.getName());

        if (m.find()) {
            return m.group(1);
        } else {
            throw new IllegalArgumentException("Unable to extract the CN attribute from " + principal.getName());
        }
    }
}
