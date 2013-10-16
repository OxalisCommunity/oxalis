package eu.peppol.as2;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.mail.smime.SMIMESignedParser;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author steinar
 *         Date: 08.10.13
 *         Time: 14:51
 */
public class MimeMessageInspector {


    private final MimeMessage mimeMessage;
    private X509Certificate signersX509Certificate;

    public MimeMessageInspector(MimeMessage mimeMessage) {
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
            throw new IllegalStateException("Unable to get content of message." +e.getMessage(),e);
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
                throw new IllegalStateException("Unable to fetch certificate for signer. "+e.getMessage(),e);
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
            BodyPart bodyPart = mimeMultipart.getBodyPart(0);
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
            if (!mimeMessage.isMimeType("multipart/signed")) {
                throw new IllegalStateException("MimeMessge is not multipart/signed " + mimeMessage.getContentType());
            }
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to retrieve content type from MimeMessage. " + e.getMessage(), e);
        }
    }

    public X509Certificate getSignersX509Certificate() {
        return signersX509Certificate;
    }
}
