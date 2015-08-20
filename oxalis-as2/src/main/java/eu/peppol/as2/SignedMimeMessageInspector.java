/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.as2;

import com.sun.xml.ws.transport.tcp.io.ByteBufferOutputStream;
import eu.peppol.security.KeystoreManager;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Inspects and provides information about a MimeMessage
 *
 * @author steinar
 *         Date: 08.10.13
 *         Time: 14:51
 */
public class SignedMimeMessageInspector {

    private static final Logger log = LoggerFactory.getLogger(SignedMimeMessageInspector.class);
    private static final String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;

    private final MimeMessage mimeMessage;
    private X509Certificate signersX509Certificate;

    public SignedMimeMessageInspector(MimeMessage mimeMessage) {
        Security.addProvider(new BouncyCastleProvider());
        this.mimeMessage = mimeMessage;
        verifyContentType();
        parseSignedMessage();
    }


    private void verifyContentType() {
        try {

            // at this stage we should have a javax.mail.internet.MimeMessage with content type text/plain
            log.debug("Verifying " + mimeMessage.getClass().getName() + " with content type " + mimeMessage.getContentType());

            // the contents of this should be a multipart MimeMultipart that is signed
            String contentType = ((MimeMultipart) mimeMessage.getContent()).getContentType();

            /*
            // Debug mimeMessage javax.mail.internet.MimeMessage with content type text/plain
            String contentType = mimeMessage.getContentType();
            if ("text/plain".equalsIgnoreCase(contentType)) {
                String content = (String) mimeMessage.getContent();
                log.debug("Verifying mimeMessage --" + contentType + "-start--" + content + "--" + contentType + "-end--");
            }
            */

            if (!contentType.startsWith("multipart/signed")) {
                throw new IllegalStateException("MimeMessage is not multipart/signed, it is : " + contentType);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve content type from MimeMessage. " + e.getMessage(), e);
        }
    }


    void parseSignedMessage() {
        SMIMESignedParser smimeSignedParser = null;
        try {
            // MimeMessageHelper.dumpMimePartToFile("/tmp/parseSignedMessage.txt", mimeMessage);
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

            // Retrieve the first certificate
            Iterator certIt = certCollection.iterator();
            if (certIt.hasNext()) {
                try {
                    signersX509Certificate = new JcaX509CertificateConverter().setProvider(PROVIDER_NAME).getCertificate((X509CertificateHolder) certIt.next());
                } catch (CertificateException e) {
                    throw new IllegalStateException("Unable to fetch certificate for signer. " + e.getMessage(), e);
                }
            } else {
                throw new IllegalStateException("Signers certificate was not found, unable to verify the signature");
            }

            // Verify that the signature is correct and that signersIterator was generated when the certificate was current
            try {
                if (!signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(PROVIDER_NAME).build(signersX509Certificate))) {
                    throw new IllegalStateException("Verification of signer failed");
                }
            } catch (CMSException e) {
                throw new IllegalStateException("Unable to verify the signer. " + e.getMessage(), e);
            } catch (OperatorCreationException e) {
                throw new IllegalStateException("Unable to verify the signer. " + e.getMessage(), e);
            }

            // Verify that the certificate issuer is trusted
            String issuerDN = signersX509Certificate.getIssuerDN().toString();
            log.debug("Verify the certificate issuer : " + issuerDN);
            validateCertificate(signersX509Certificate);

        } else {
            throw new IllegalStateException("There is no signer information available");
        }

    }

    private void validateCertificate(X509Certificate certificate) {

        try {

            List<X509Certificate> certificateList = new ArrayList<X509Certificate>();
            certificateList.add(certificate);

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            CertPath certPath = certificateFactory.generateCertPath(certificateList);

            // Create the parameters for the validator
            KeystoreManager keystoreManager = KeystoreManager.getInstance();
            PKIXParameters params = new PKIXParameters(keystoreManager.getPeppolTruststore());

            // Disable revocation checking as we trust our own truststore (and do not have a CRL and don't want OCSP)
            params.setRevocationEnabled(false);

            // Validate the certificate path
            CertPathValidator pathValidator = CertPathValidator.getInstance("PKIX",PROVIDER_NAME);
            CertPathValidatorResult validatorResult = pathValidator.validate(certPath, params);

            // Get the CA used to validate this path
            PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) validatorResult;
            TrustAnchor ta = result.getTrustAnchor();
            X509Certificate trustCert = ta.getTrustedCert();

            log.debug("Trusted cert was : {}", trustCert.getSubjectDN().toString());

        } catch (Exception e) {
            throw new IllegalStateException("Unable to trust the signer : " + e.getMessage(), e);
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

    public MimeMessage getMimeMessage() {
        return mimeMessage;
    }

    public X509Certificate getSignersX509Certificate() {
        return signersX509Certificate;
    }

    public Mic calculateMic(String algorithmName) {
        try {

            MessageDigest messageDigest = MessageDigest.getInstance(algorithmName, PROVIDER_NAME);

            MimeMultipart mimeMultipart = (MimeMultipart) mimeMessage.getContent();
            BodyPart bodyPart = mimeMultipart.getBodyPart(0);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bodyPart.writeTo(baos);
            // bodyPart.writeTo(System.out);
            byte[] content = baos.toByteArray();
            messageDigest.update(content);
            String digestAsString = new String(Base64.encode(messageDigest.digest()));
            return new Mic(digestAsString, algorithmName);

             /*
            InputStream resourceAsStream = getPayload() / getInputStreamForMimeMessage();
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
            */

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithmName + " not found", e);
        } catch (NoSuchProviderException e) {
            throw new IllegalStateException("Security provider " + PROVIDER_NAME + " not found. Do you have BouncyCastle on your path?");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read data from digest input. " + e.getMessage(), e);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to handle mime body part. " + e.getMessage(), e);
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