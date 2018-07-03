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

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;

/**
 * @author erlend
 * @since 4.0.0
 */
public class CertificateUtils {

    /**
     * From https://stackoverflow.com/a/8322929/135001
     *
     * @param cert X.509 Certificate
     * @return Extracted Common Name from certificate.
     */
    public static String extractCommonName(X509Certificate cert) {
        X500Principal principal = cert.getSubjectX500Principal();

        X500Name x500name = new X500Name(principal.getName());
        RDN cn = x500name.getRDNs(BCStyle.CN)[0];

        return IETFUtils.valueToString(cn.getFirst().getValue());
    }
}
