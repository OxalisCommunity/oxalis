/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.as2;

import eu.peppol.MessageDigestResult;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESignedParser;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

/**
 * Represents an S/MIME message, which provides meta information and data from the signed MimeMessage.
 *
 * @author steinar
 *         Date: 08.10.13
 *         Time: 14:51
 */
public class SignedMimeMessage {

    private static final Logger log = LoggerFactory.getLogger(SignedMimeMessage.class);

    private final MimeMessage mimeMessage;
    private X509Certificate signersX509Certificate;


    public SignedMimeMessage(MimeMessage mimeMessage) {

        // Installs the Bouncy Castle provider
        Security.addProvider(new BouncyCastleProvider());

        this.mimeMessage = mimeMessage;
        verifyContentType();

        parseSignedMessage();
    }


    /** Provides an InputStream referencing the payload of the S/MIME message.
     * This includes the entire payload, including the SBDH.
     *
     * @return inputStream pointing to the first byte of the payload.
     */
    public InputStream getPayload() {
        try {
            MimeMultipart mimeMultipart = (MimeMultipart) mimeMessage.getContent();
            BodyPart bodyPart = mimeMultipart.getBodyPart(0);   // First part contains the data, second contains the signature
            return bodyPart.getInputStream();
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

            MessageDigest messageDigest = MessageDigest.getInstance(algorithmName, new BouncyCastleProvider());

            MimeMultipart mimeMultipart = (MimeMultipart) mimeMessage.getContent();

            BodyPart bodyPart = mimeMultipart.getBodyPart(0);
/*
            System.out.println("----- Body part to calculate Mic from: -----");
            bodyPart.writeTo(System.out);
            System.out.println("-----  -----------");
*/
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bodyPart.writeTo(baos); // Writes the entire contents of first multipart, including the MIME headers

            byte[] content = baos.toByteArray();
            messageDigest.update(content);
            String digestAsString = new String(Base64.encode(messageDigest.digest()));

            return new Mic(digestAsString, algorithmName);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithmName + " not found", e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read data from digest input. " + e.getMessage(), e);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to handle mime body part. " + e.getMessage(), e);
        }
    }

    /**
     * Calculates the message digest of the payload
     *
     * @return the Message digest for the payload
     */
    public MessageDigestResult calcPayloadDigest(String algorithmName) {

        MessageDigest instance;
        try {
            instance = MessageDigest.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to create message digester " + e.getMessage(), e);
        }
        DigestInputStream digestInputStream = new DigestInputStream(getPayload(), instance);
        try {
            while ((digestInputStream.read()) >= 0) {
                ;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading Mime message payload for calculating digest." + e.getMessage(), e);
        }

        return new MessageDigestResult(instance.digest(), instance.getAlgorithm());
    }


    private void verifyContentType() {
        try {

            // at this stage we should have a javax.mail.internet.MimeMessage with content type text/plain
            log.debug("Verifying " + mimeMessage.getClass().getName() + " with content type " + mimeMessage.getContentType());

            // the contents of this should be a multipart MimeMultipart that is signed
            String contentType = ((MimeMultipart) mimeMessage.getContent()).getContentType();

            if (!contentType.startsWith("multipart/signed")) {
                throw new IllegalStateException("MimeMessage is not multipart/signed, it is : " + contentType);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve content type from MimeMessage. " + e.getMessage(), e);
        }
    }


    void parseSignedMessage() {
        SMIMESignedParser smimeSignedParser;
        try {
            // MimeMessageHelper.dumpMimePartToFile("/tmp/parseSignedMessage.txt", mimeMessage);
            smimeSignedParser = new SMIMESignedParser(new JcaDigestCalculatorProviderBuilder().build(),(MimeMultipart) mimeMessage.getContent());
        } catch (MessagingException | CMSException | IOException | OperatorCreationException e) {
            throw new IllegalStateException("Unable to create SMIMESignedParser: " + e.getMessage(), e);
        }

        Store certs;
        try {
            certs = smimeSignedParser.getCertificates();
        } catch (CMSException e) {
            throw new IllegalStateException("Unable to retrieve the certificates from signed message.");
        }

        //
        // SignerInfo blocks which contain the signatures
        //
        SignerInformationStore signerInfos;
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
            @SuppressWarnings("unchecked")
            Collection certCollection = certs.getMatches(signer.getSID());

            // Retrieve the first certificate
            Iterator certIt = certCollection.iterator();
            if (certIt.hasNext()) {
                try {
                    signersX509Certificate = new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate((X509CertificateHolder) certIt.next());
                } catch (CertificateException e) {
                    throw new IllegalStateException("Unable to fetch certificate for signer. " + e.getMessage(), e);
                }
            } else {
                throw new IllegalStateException("Signers certificate was not found, unable to verify the signature");
            }

            // Verify that the signature is correct and that signersIterator was generated when the certificate was current
            try {
                if (!signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(new BouncyCastleProvider()).build(signersX509Certificate))) {
                    throw new IllegalStateException("Verification of signer failed");
                }
            } catch (CMSException e) {
                throw new IllegalStateException("Unable to verify the signer. " + e.getMessage(), e);
            } catch (OperatorCreationException e) {
                throw new IllegalStateException("Unable to verify the signer. " + e.getMessage(), e);
            }

            String issuerDN = signersX509Certificate.getIssuerDN().toString();
            log.debug("Certificate issued by: " + issuerDN);

        } else {
            throw new IllegalStateException("There is no signer information available");
        }

    }
}