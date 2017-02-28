/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package no.difi.oxalis.as2.util;

import no.difi.vefa.peppol.common.code.DigestMethod;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;

public enum SMimeDigestMethod {
    // md5("md5", "MD5"),
    // rsa_md5("rsa-md5", "MD5"),
    sha1("sha1", "SHA-1", OIWObjectIdentifiers.idSHA1, DigestMethod.SHA1, TransportProfile.AS2_1_0),
    sha_1("sha-1", "SHA-1", OIWObjectIdentifiers.idSHA1, DigestMethod.SHA1, TransportProfile.AS2_1_0),
    rsa_sha1("rsa-sha1", "SHA-1", OIWObjectIdentifiers.idSHA1, DigestMethod.SHA1, TransportProfile.AS2_1_0),
    // sha256("sha256", "SHA-256"),
    // sha384("sha384", "SHA-384"),
    sha512("sha512", "SHA-512", NISTObjectIdentifiers.id_sha512, DigestMethod.SHA512,
            TransportProfile.of("busdox-transport-as2-ver1p0r1"));

    private final String identifier;

    private final String method;

    private final ASN1ObjectIdentifier oid;

    private final DigestMethod digestMethod;

    private final TransportProfile transportProfile;

    SMimeDigestMethod(String identifier, String method, ASN1ObjectIdentifier oid,
                      DigestMethod digestMethod, TransportProfile transportProfile) {
        this.identifier = identifier;
        this.method = method;
        this.oid = oid;
        this.digestMethod = digestMethod;
        this.transportProfile = transportProfile;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getMethod() {
        return method;
    }

    public ASN1ObjectIdentifier getOid() {
        return oid;
    }

    public DigestMethod getDigestMethod() {
        return digestMethod;
    }

    public TransportProfile getTransportProfile() {
        return transportProfile;
    }

    public static SMimeDigestMethod findByIdentifier(String identifier) {
        for (SMimeDigestMethod digestMethod : values())
            if (digestMethod.getIdentifier().equals(identifier))
                return digestMethod;

        throw new IllegalArgumentException(String.format("Digest method '%s' not known.", identifier));
    }
}

