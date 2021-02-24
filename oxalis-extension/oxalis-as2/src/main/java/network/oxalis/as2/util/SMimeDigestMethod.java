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

package network.oxalis.as2.util;

import network.oxalis.vefa.peppol.common.code.DigestMethod;
import network.oxalis.vefa.peppol.common.model.TransportProfile;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;

import java.security.NoSuchAlgorithmException;

public enum SMimeDigestMethod {

    sha1(new String[]{"sha1", "sha-1", "rsa-sha1"}, "SHA1withRSA", "SHA-1", OIWObjectIdentifiers.idSHA1,
            DigestMethod.SHA1, TransportProfile.PEPPOL_AS2_1_0),

    sha256(new String[]{"sha256", "sha-256"}, "SHA256withRSA", "SHA-256", NISTObjectIdentifiers.id_sha256,
            DigestMethod.SHA256, TransportProfile.PEPPOL_AS2_2_0),

    sha512(new String[]{"sha512", "sha-512"}, "SHA512withRSA", "SHA-512", NISTObjectIdentifiers.id_sha512,
            DigestMethod.SHA512, TransportProfile.of("busdox-transport-as2-ver1p0r1")),

    ;

    private final String[] identifier;

    private final String method;

    private final String algorithm;

    private final ASN1ObjectIdentifier oid;

    private final DigestMethod digestMethod;

    private final TransportProfile transportProfile;

    SMimeDigestMethod(String[] identifier, String method, String algorithm, ASN1ObjectIdentifier oid,
                      DigestMethod digestMethod, TransportProfile transportProfile) {
        this.identifier = identifier;
        this.method = method;
        this.algorithm = algorithm;
        this.oid = oid;
        this.digestMethod = digestMethod;
        this.transportProfile = transportProfile;
    }

    public String getIdentifier() {
        return identifier[0];
    }

    public String getMethod() {
        return method;
    }

    public String getAlgorithm() {
        return algorithm;
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

    public static SMimeDigestMethod findByIdentifier(String identifier) throws NoSuchAlgorithmException {
        String provided = String.valueOf(identifier).toLowerCase();

        for (SMimeDigestMethod digestMethod : values())
            for (String ident : digestMethod.identifier)
                if (ident.equals(provided))
                    return digestMethod;

        throw new NoSuchAlgorithmException(String.format(
                "Digest method '%s' not known.", identifier));
    }

    public static SMimeDigestMethod findByTransportProfile(TransportProfile transportProfile) {
        for (SMimeDigestMethod digestMethod : values())
            if (digestMethod.transportProfile.equals(transportProfile))
                return digestMethod;

        throw new IllegalArgumentException(String.format(
                "Digest method for transport profile '%s' not known.", transportProfile));
    }

    public static SMimeDigestMethod findByDigestMethod(DigestMethod digestMethod) {
        for (SMimeDigestMethod method : values())
            if (method.digestMethod.equals(digestMethod))
                return method;

        throw new IllegalArgumentException(String.format(
                "Digest method '%s' not known.", digestMethod));
    }
}

