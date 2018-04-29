/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.commons.security;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author erlend
 * @since 4.0.0
 */
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
