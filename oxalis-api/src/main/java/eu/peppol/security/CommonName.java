/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.security;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
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

    private static final Pattern PATTERN = Pattern.compile("CN=([^,]*),");

    private final X509Certificate certificate;

    private final String value;

    public CommonName(String value) {
        this(value, null);
    }

    public CommonName(String value, X509Certificate certificate) {
        this.value = value;
        this.certificate = certificate;
    }

    /**
     * Creates a CommonName instance by extracting the Common Name (CN) attribute from the supplied X509Certificate
     *
     * @param certificate the certificate from which we extract the common name attribute
     */
    public static CommonName of(X509Certificate certificate) {
        X500Principal principal = certificate.getSubjectX500Principal();
        String distinguishedName = principal.getName();
        Matcher m = PATTERN.matcher(distinguishedName);
        if (m.find()) {
            String commonNameTextValue = m.group(1);
            return new CommonName(commonNameTextValue);
        } else {
            throw new IllegalArgumentException("Unable to extract the CN attribute from " + principal.getName());
        }
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommonName that = (CommonName) o;

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
