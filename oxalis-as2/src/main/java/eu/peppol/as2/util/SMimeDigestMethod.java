package eu.peppol.as2.util;

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

    SMimeDigestMethod(String identifier, String method, ASN1ObjectIdentifier oid, DigestMethod digestMethod, TransportProfile transportProfile) {
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