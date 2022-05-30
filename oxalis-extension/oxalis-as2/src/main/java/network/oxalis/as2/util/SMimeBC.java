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

import network.oxalis.api.lang.OxalisSecurityException;
import network.oxalis.commons.bouncycastle.BCHelper;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERUTCTime;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;

public class SMimeBC {

    static {
        BCHelper.registerProvider();
    }

    public static byte[] createSignature(byte[] digest, SMimeDigestMethod digestMethod, PrivateKey privateKey,
                                         X509Certificate certificate) throws OxalisSecurityException {
        try {
            ASN1EncodableVector signedAttributes = new ASN1EncodableVector();
            signedAttributes.add(new Attribute(CMSAttributes.contentType, new DERSet(digestMethod.getOid())));
            signedAttributes.add(new Attribute(CMSAttributes.messageDigest, new DERSet(new DEROctetString(digest))));
            signedAttributes.add(new Attribute(CMSAttributes.signingTime, new DERSet(new DERUTCTime(new Date()))));

            AttributeTable signedAttributesTable = new AttributeTable(signedAttributes);
            signedAttributesTable.toASN1EncodableVector();
            DefaultSignedAttributeTableGenerator signedAttributeGenerator = new DefaultSignedAttributeTableGenerator(signedAttributesTable);

            /* Build the SignerInfo generator builder, that will build the generator... that will generate the SignerInformation... */
            SignerInfoGeneratorBuilder signerInfoBuilder = new SignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build());
            signerInfoBuilder.setSignedAttributeGenerator(signedAttributeGenerator);
            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
            JcaContentSignerBuilder contentSigner =
                    new JcaContentSignerBuilder(digestMethod.getMethod()).setProvider(BouncyCastleProvider.PROVIDER_NAME);

            generator.addSignerInfoGenerator(signerInfoBuilder.build(
                    contentSigner.build(privateKey), new X509CertificateHolder(certificate.getEncoded())));
            generator.addCertificates(new JcaCertStore(Collections.singletonList(certificate)));

            return generator.generate(new CMSAbsentContent()).getEncoded();
        } catch (CMSException | IOException | CertificateEncodingException | OperatorCreationException e) {
            throw new OxalisSecurityException(e.getMessage(), e);
        }
    }
}
