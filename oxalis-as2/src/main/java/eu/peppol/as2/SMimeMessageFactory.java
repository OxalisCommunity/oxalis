/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
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

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;

import javax.activation.MimeType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Creates signed Mime messages
 */
public class SMimeMessageFactory {

    private final PrivateKey privateKey;
    private final X509Certificate ourCertificate;

    public SMimeMessageFactory(PrivateKey privateKey, X509Certificate ourCertificate) {
        this.privateKey = privateKey;
        this.ourCertificate = ourCertificate;
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    /**
     * Creates an S/MIME message from the supplied String, having the supplied MimeType as the "content-type".
     *
     * @param msg holds the payload of the message
     * @param mimeType the MIME type to be used as the "Content-Type"
     */
    public MimeMessage createSignedMimeMessage(final String msg, MimeType mimeType) {
        return createSignedMimeMessage(new ByteArrayInputStream(msg.getBytes()), mimeType);
    }

    /** Creates a new S/MIME message having the supplied MimeType as the "content-type" */
    public MimeMessage createSignedMimeMessage(final InputStream inputStream, MimeType mimeType) {
        MimeBodyPart mimeBodyPart = MimeMessageHelper.createMimeBodyPart(inputStream, mimeType);
        return createSignedMimeMessage(mimeBodyPart);
    }

    /** Creates an S/MIME message using the supplied MimeBodyPart. The signature is generated using the private key
     * as supplied in the constructor. Our certificate, which is required to verify the signature is enclosed.
     */
    public MimeMessage createSignedMimeMessage(MimeBodyPart mimeBodyPart)  {

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
            smimeSignedGenerator.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider(new BouncyCastleProvider()).setSignedAttributeGenerator(new AttributeTable(signedAttrs)).build("SHA1withRSA", privateKey, ourCertificate));
        } catch (OperatorCreationException e) {
            throw new IllegalStateException("Unable to add Signer information. " + e.getMessage(), e);
        } catch (CertificateEncodingException e) {
            throw new IllegalStateException("Certificate encoding problems while adding signer information." + e.getMessage(), e);
        }

        //
        // add our pool of certs and crls (if any) to go with the signature
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

}
