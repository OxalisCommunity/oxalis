package eu.peppol.as2;

import eu.peppol.security.KeystoreManager;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jcajce.provider.keystore.BC;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.Store;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MimeType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Creates signed Mime messages
 */
public class MimeMessageFactory {

    private final PrivateKey privateKey;
    private final X509Certificate ourCertificate;

    public MimeMessageFactory(PrivateKey privateKey, X509Certificate ourCertificate) {
        this.privateKey = privateKey;
        this.ourCertificate = ourCertificate;

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }


    public MimeMessage createSignedMimeMessage(final String msg, MimeType mimeType) {
        return createSignedMimeMessage(new ByteArrayInputStream(msg.getBytes()), mimeType);
    }

    public MimeMessage createSignedMimeMessage(final InputStream inputStream, MimeType mimeType) {

        MimeBodyPart mimeBodyPart = createMimeBodyPart(inputStream, mimeType);
        return createSignedMimeMessage(mimeBodyPart);
    }

    public MimeMessage createSignedMimeMessage(MimeBodyPart mimeBodyPart)  {

        //
        // S/MIME capabilities are required, but we simply supply an empty vector
        //
        ASN1EncodableVector         signedAttrs = new ASN1EncodableVector();

        //
        // create the generator for creating an smime/signed message
        //
        SMIMESignedGenerator smimeSignedGenerator = new SMIMESignedGenerator();

        //
        // add a signer to the generator - this specifies we are using SHA1 and
        // adding the smime attributes above to the signed attributes that
        // will be generated as part of the signature. The encryption algorithm
        // used is taken from the key - in this RSA with PKCS1Padding
        //
        try {
            smimeSignedGenerator.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider("BC").setSignedAttributeGenerator(new AttributeTable(signedAttrs)).build("SHA1withRSA", privateKey, ourCertificate));
        } catch (OperatorCreationException e) {
            throw new IllegalStateException("Unable to add Signer information. " + e.getMessage(), e);
        } catch (CertificateEncodingException e) {
            throw new IllegalStateException("Certificate encoding problems while adding signer information." + e.getMessage(), e);
        }

        //
        // add our pool of certs and cerls (if any) to go with the signature
        //
        List certList = new ArrayList();
        certList.add(ourCertificate);

        //
        // create a CertStore containing the certificates we want carried
        // in the signature
        //
        Store certs = null;
        try {
            certs = new JcaCertStore(certList);
        } catch (CertificateEncodingException e) {
            throw new IllegalStateException("Unable to create JcaCertStore with our certificate. " + e.getMessage(), e);
        }
        smimeSignedGenerator.addCertificates(certs);

        //
        // Signs the supplied MimeBodyPart
        //
        MimeMultipart mimeMultipart = null;
        try {
            mimeMultipart = smimeSignedGenerator.generate(mimeBodyPart);
        } catch (SMIMEException e) {
            throw new IllegalStateException("Unable to generate signed mime multipart." + e.getMessage(), e);
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
            throw new IllegalStateException("Unable to  set Content type of MimeMessage. " + e.getMessage(), e);
        }
        try {
            mimeMessage.saveChanges();
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to save changes to Mime message. " + e.getMessage(), e);
        }

        return mimeMessage;
    }



    MimeBodyPart createMimeBodyPart(InputStream inputStream, MimeType mimeType) {
        MimeBodyPart mimeBodyPart = new MimeBodyPart();


        ByteArrayDataSource byteArrayDataSource = null;
        try {
            byteArrayDataSource = new ByteArrayDataSource(inputStream, mimeType.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create ByteArrayDataSource from inputStream." + e.getMessage(), e);
        }


        try {
            mimeBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to set data handler on mime body part." + e.getMessage(), e);
        }

        try {
            mimeBodyPart.setHeader("Content-Type", mimeType.toString());
            mimeBodyPart.setHeader("Content-Transfer-Encoding", "binary");   // No content-transfer-encoding needed for http
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to set headers." + e.getMessage(), e);
        }

        return mimeBodyPart;
    }


}
