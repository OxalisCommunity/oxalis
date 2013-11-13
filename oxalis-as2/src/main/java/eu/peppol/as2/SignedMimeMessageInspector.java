package eu.peppol.as2;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESignedParser;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

/**
 * Inspects and provides information about a MimeMessage
 *
 * @author steinar
 *         Date: 08.10.13
 *         Time: 14:51
 */
public class SignedMimeMessageInspector {

    private final MimeMessage mimeMessage;
    private X509Certificate signersX509Certificate;

    public SignedMimeMessageInspector(MimeMessage mimeMessage) {
        Security.addProvider(new BouncyCastleProvider());

        this.mimeMessage = mimeMessage;
        parseSignedMessage();
    }

    public MimeMessage getMimeMessage() {
        return mimeMessage;
    }

    void parseSignedMessage() {
        verifyContentType();

        SMIMESignedParser smimeSignedParser = null;
        try {
            smimeSignedParser = new SMIMESignedParser((MimeMultipart) mimeMessage.getContent());
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to get content of message." + e.getMessage(), e);
        } catch (CMSException e) {
            throw new IllegalStateException("Unable to get content of message. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to get content of message. " + e.getMessage(), e);
        }


        Store certs = null;
        try {
            certs = smimeSignedParser.getCertificates();
        } catch (CMSException e) {
            throw new IllegalStateException("Unable to retrieve the certificates from signed message.");
        }

        //
        // SignerInfo blocks which contain the signatures
        //
        SignerInformationStore signerInfos = null;
        try {
            signerInfos = smimeSignedParser.getSignerInfos();
        } catch (CMSException e) {
            throw new IllegalStateException("Unable to get the Signer information from message. " + e.getMessage(), e);
        }

        Collection signers = signerInfos.getSigners();
        Iterator signersIterator = signers.iterator();

        //
        // Only a single signer, get the first and only certificate
        //
        if (signersIterator.hasNext()) {
            // Retrieves information on first and only signer
            SignerInformation signer = (SignerInformation) signersIterator.next();

            // Retrieves the collection of certificates for first and only signer
            Collection certCollection = certs.getMatches(signer.getSID());

            Iterator certIt = certCollection.iterator();
            try {
                signersX509Certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate((X509CertificateHolder) certIt.next());
            } catch (CertificateException e) {
                throw new IllegalStateException("Unable to fetch certificate for signer. " + e.getMessage(), e);
            }

            //
            // verify that the sig is correct and that signersIterator was generated
            // when the certificate was current
            //
            try {
                if (!signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(signersX509Certificate))) {
                    throw new IllegalStateException("Verification of signer failed");
                }
            } catch (CMSException e) {
                throw new IllegalStateException("Unable to verify the signer. " + e.getMessage(), e);
            } catch (OperatorCreationException e) {
                throw new IllegalStateException("Unable to verify the signer. " + e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("There is no signer information available");
        }

    }

    public InputStream getPayload() {
        try {
            MimeMultipart mimeMultipart = (MimeMultipart) mimeMessage.getContent();
            BodyPart bodyPart = mimeMultipart.getBodyPart(0);   // First part contains the data, second contains the signature
            InputStream inputStream = bodyPart.getInputStream();
            return inputStream;

        } catch (IOException e) {
            throw new IllegalStateException("Unable to access the contents of the payload in first body part. " + e.getMessage(), e);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to access the contents of the payload in first body part. " + e.getMessage(), e);
        }
    }

    private void verifyContentType() {
        try {
            String contentType = ((MimeMultipart) mimeMessage.getContent()).getContentType();

            if (!contentType.startsWith("multipart/signed")) {
                throw new IllegalStateException("MimeMessge is not multipart/signed, it is:" + mimeMessage.getContentType());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve content type from MimeMessage. " + e.getMessage(), e);
        }
    }

    public X509Certificate getSignersX509Certificate() {
        return signersX509Certificate;
    }

    public Mic calculateMic(String algorithmName) {
        MessageDigest messageDigest = null;

        String providerName = "BC";
        try {
            messageDigest = MessageDigest.getInstance(algorithmName, providerName);
            InputStream resourceAsStream = getInputStreamForMimeMessage();

            DigestInputStream digestInputStream = new DigestInputStream(resourceAsStream, messageDigest);

            // Reads through the entire file in order to create the digest
            final byte[] aBuf = new byte[4096];
            while (digestInputStream.read(aBuf) >= 0) {
                digestInputStream.close();
            }

            // grabs the digest after reading all of the contents.
            byte[] digest = digestInputStream.getMessageDigest().digest();
            String digestAsString = new String(Base64.encode(digest));

            return new Mic(digestAsString, algorithmName);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithmName + " not found", e);
        } catch (NoSuchProviderException e) {
            throw new IllegalStateException("Security provider " + providerName + " not found. Do you have BouncyCastle on your path?");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read data from digest input. " + e.getMessage(), e);
        }
    }

    private InputStream getInputStreamForMimeMessage() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            getMimeMessage().writeTo(baos);

        } catch (IOException e) {
            throw new IllegalStateException("Unable to write MIME message to byte array output stream: " + e.getMessage(), e);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to read contents of MIME message; " + e.getMessage(), e);
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

}