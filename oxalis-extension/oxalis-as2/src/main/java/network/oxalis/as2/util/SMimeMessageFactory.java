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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import network.oxalis.api.lang.OxalisSecurityException;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.commons.bouncycastle.BCHelper;
import network.oxalis.vefa.peppol.common.model.Digest;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimeType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Properties;

/**
 * Creates signed Mime messages
 */
@Singleton
public class SMimeMessageFactory {

    private final PrivateKey privateKey;

    private final X509Certificate ourCertificate;

    private final static Session session = Session.getDefaultInstance(System.getProperties(), null);

    static {
        BCHelper.registerProvider();
    }

    @Inject
    public SMimeMessageFactory(PrivateKey privateKey, X509Certificate ourCertificate) {
        this.privateKey = privateKey;
        this.ourCertificate = ourCertificate;
    }

    /**
     * Creates an S/MIME message from the supplied String, having the supplied MimeType as the "content-type".
     *
     * @param msg      holds the payload of the message
     * @param mimeType the MIME type to be used as the "Content-Type"
     */
    public MimeMessage createSignedMimeMessage(final String msg, MimeType mimeType, SMimeDigestMethod digestMethod)
            throws OxalisTransmissionException {
        return createSignedMimeMessage(new ByteArrayInputStream(msg.getBytes()), mimeType, digestMethod);
    }

    /**
     * Creates a new S/MIME message having the supplied MimeType as the "content-type"
     */
    public MimeMessage createSignedMimeMessage(final InputStream inputStream, MimeType mimeType,
                                               SMimeDigestMethod digestMethod) throws OxalisTransmissionException {
        MimeBodyPart mimeBodyPart = MimeMessageHelper.createMimeBodyPart(inputStream, mimeType.toString());
        return createSignedMimeMessage(mimeBodyPart, digestMethod);
    }

    /**
     * Creates an S/MIME message using the supplied MimeBodyPart. The signature is generated using the private key
     * as supplied in the constructor. Our certificate, which is required to verify the signature is enclosed.
     */
    public MimeMessage createSignedMimeMessage(MimeBodyPart mimeBodyPart, SMimeDigestMethod digestMethod)
            throws OxalisTransmissionException {

        //
        // S/MIME capabilities are required, but we simply supply an empty vector
        //
        ASN1EncodableVector signedAttrs = new ASN1EncodableVector();

        //
        // create the generator for creating an smime/signed message
        //
        SMIMESignedGenerator smimeSignedGenerator = new SMIMESignedGenerator("binary"); //also see CMSSignedGenerator ?

        //
        // add a signer to the generator - this specifies we are using SHA1 and
        // adding the smime attributes above to the signed attributes that
        // will be generated as part of the signature. The encryption algorithm
        // used is taken from the key - in this RSA with PKCS1Padding
        //
        try {
            smimeSignedGenerator.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .setSignedAttributeGenerator(new AttributeTable(signedAttrs))
                    // .build("SHA1withRSA", privateKey, ourCertificate));
                    .build(digestMethod.getMethod(), privateKey, ourCertificate));
        } catch (OperatorCreationException e) {
            throw new OxalisTransmissionException("Unable to add Signer information. " + e.getMessage(), e);
        } catch (CertificateEncodingException e) {
            throw new OxalisTransmissionException(String.format(
                    "Certificate encoding problems while adding signer information. %s", e.getMessage()), e);
        }

        //
        // create a CertStore containing the certificates we want carried
        // in the signature
        //
        Store certs;
        try {
            certs = new JcaCertStore(Collections.singleton(ourCertificate));
        } catch (CertificateEncodingException e) {
            throw new OxalisTransmissionException("Unable to create JcaCertStore with our certificate. " + e.getMessage(), e);
        }
        smimeSignedGenerator.addCertificates(certs);

        //
        // Signs the supplied MimeBodyPart
        //
        MimeMultipart mimeMultipart;
        try {
            mimeMultipart = smimeSignedGenerator.generate(mimeBodyPart);
        } catch (SMIMEException e) {
            throw new OxalisTransmissionException("Unable to generate signed mime multipart." + e.getMessage(), e);
        }

        //
        // Get a Session object and create the mail message
        //
        Properties props = System.getProperties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage mimeMessage = new MimeMessage(session);

        try {
            mimeMessage.setContent(mimeMultipart, mimeMultipart.getContentType());
        } catch (MessagingException e) {
            throw new OxalisTransmissionException("Unable to  set Content type of MimeMessage. " + e.getMessage(), e);
        }
        try {
            mimeMessage.saveChanges();
        } catch (MessagingException e) {
            throw new OxalisTransmissionException("Unable to save changes to Mime message. " + e.getMessage(), e);
        }

        return mimeMessage;
    }

    public MimeMessage createSignedMimeMessageNew(MimeBodyPart mimeBodyPart, Digest digest, SMimeDigestMethod digestMethod)
            throws OxalisTransmissionException {
        try {
            MimeMultipart mimeMultipart = new MimeMultipart();
            mimeMultipart.setSubType("signed");
            mimeMultipart.addBodyPart(mimeBodyPart);

            MimeBodyPart signaturePart = new MimeBodyPart();
            DataSource dataSource = new ByteArrayDataSource(SMimeBC.createSignature(digest.getValue(), digestMethod, privateKey, ourCertificate), "application/pkcs7-signature");
            signaturePart.setDataHandler(new DataHandler(dataSource));
            signaturePart.setHeader("Content-Type", "application/pkcs7-signature; name=smime.p7s; smime-type=signed-data");
            signaturePart.setHeader("Content-Transfer-Encoding", "base64");
            signaturePart.setHeader("Content-Disposition", "attachment; filename=\"smime.p7s\"");
            signaturePart.setHeader("Content-Description", "S/MIME Cryptographic Signature");
            mimeMultipart.addBodyPart(signaturePart);

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setContent(mimeMultipart, mimeMultipart.getContentType());
            mimeMessage.saveChanges();

            return mimeMessage;
        } catch (MessagingException | OxalisSecurityException e) {
            throw new OxalisTransmissionException(e.getMessage(), e);
        }
    }
}
