package network.oxalis.as2.util;

import com.google.common.io.ByteStreams;
import com.sun.mail.util.LineOutputStream;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.lang.OxalisSecurityException;
import network.oxalis.as2.lang.OxalisAs2Exception;
import network.oxalis.commons.bouncycastle.BCHelper;
import network.oxalis.commons.security.CertificateUtils;
import network.oxalis.vefa.peppol.common.code.Service;
import network.oxalis.vefa.peppol.security.api.CertificateValidator;
import network.oxalis.vefa.peppol.security.lang.PeppolSecurityException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.CollectionStore;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * @author erlend
 */
@Slf4j
public class SignedMessage {

    private static final Session SESSION = Session.getDefaultInstance(System.getProperties());

    private MimeMultipart mimeMultipart;

    private SMIMESigned smimeSigned;

    // private List<Header> headers;

    private byte[] signature;

    private String micalg;

    private X509Certificate signer;

    private byte[] digest;

    static {
        BCHelper.registerProvider();
    }

    public static SignedMessage load(InputStream inputStream) throws IOException, MessagingException, OxalisAs2Exception {
        return new SignedMessage(new MimeMessage(SESSION, inputStream));
    }

    public static SignedMessage load(MimeMessage mimeMessage) throws IOException, OxalisAs2Exception {
        return new SignedMessage(mimeMessage);
    }

    private SignedMessage(MimeMessage message) throws IOException, OxalisAs2Exception {
        try {
            // Verify content type
            if (!message.isMimeType("multipart/signed"))
                throw new OxalisAs2Exception("Received content is not 'multipart/signed'.");

            micalg = extractMicalg(message);

            // Extract headers
            //noinspection unchecked
            // headers = Collections.list((Enumeration<Header>) message.getAllHeaders());

            // Create MimeMultitype
            mimeMultipart = (MimeMultipart) message.getContent();

            // Extracting signature
            signature = ByteStreams.toByteArray(mimeMultipart.getBodyPart(1).getInputStream());

            // Create signed message
            smimeSigned = new SMIMESigned(mimeMultipart);
        } catch (CMSException | MessagingException e) {
            throw new OxalisAs2Exception("Unable to parse received content.", e);
        }
    }

    public InputStream getContent() throws IOException, OxalisSecurityException, OxalisAs2Exception {
        try {
            if (signer == null)
                throw new OxalisSecurityException("Content is not validated.");

            return smimeSigned.getContent().getInputStream();
        } catch (MessagingException e) {
            throw new OxalisAs2Exception("Unable to fetch content.", e);
        }
    }

    public byte[] getContentBytes() throws IOException, OxalisSecurityException, OxalisAs2Exception {
        return ByteStreams.toByteArray(getContent());
    }

    public String getMicalg() {
        return micalg;
    }

    public X509Certificate getSigner() {
        return signer;
    }

    public byte[] getDigest() {
        return digest;
    }

    public byte[] getSignature() {
        return signature;
    }

    /**
     * Extracts headers of body MIME part. Creates headers as done by Bouncycastle.
     *
     * @return Headers
     */
    public byte[] getBodyHeader() throws IOException, OxalisAs2Exception {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            LineOutputStream los = new LineOutputStream(outputStream);

            Enumeration hdrLines = ((MimeBodyPart) mimeMultipart.getBodyPart(0)).getNonMatchingHeaderLines(new String[]{});
            while (hdrLines.hasMoreElements())
                los.writeln((String) hdrLines.nextElement());

            // The CRLF separator between header and content
            los.writeln();
            los.close();

            return outputStream.toByteArray();
        } catch (MessagingException e) {
            throw new OxalisAs2Exception("Unable to fetch body headers.", e);
        }
    }


    public void validate(X509Certificate certificate) throws OxalisSecurityException, PeppolSecurityException {
        try {
            SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build(certificate.getPublicKey());

            for (SignerInformation signerInformation : smimeSigned.getSignerInfos().getSigners()) {
                if (signerInformation.verify(verifier)) {
                    signer = certificate;
                    digest = signerInformation.getContentDigest();
                    return;
                }
            }
        } catch (CMSException e) {
            throw new OxalisSecurityException(e.getMessage(), e);
        } catch (OperatorCreationException e) {
            throw new OxalisSecurityException("Unable to create SignerInformationVerifier.", e);
        }

        throw new PeppolSecurityException("Unable to verify signature.");
    }

    public void validate(Service service, CertificateValidator validator)
            throws IOException, OxalisSecurityException, PeppolSecurityException {
        validate(service, validator, null);
    }

    public void validate(Service service, CertificateValidator validator, String commonName)
            throws IOException, OxalisSecurityException, PeppolSecurityException {
        for (X509CertificateHolder holder : (CollectionStore<X509CertificateHolder>) smimeSigned.getCertificates()) {
            if (CertificateUtils.containsCommonName(holder.getSubject(), commonName)) {
                try {
                    X509Certificate certificate = CertificateUtils.parseCertificate(holder.getEncoded());

                    if (isValid(service, validator, certificate)) {
                        validate(certificate);
                        return;
                    }
                } catch (CertificateException e) {
                    log.debug("Unable to initiate certificate object.");
                }
            }
        }

        throw new OxalisSecurityException(commonName == null ?
                "Unable to find valid certificate for validation of content." :
                String.format("Unable to find valid certificate with CN '%s' for validation of content.", commonName));
    }

    private boolean isValid(Service service, CertificateValidator validator, X509Certificate certificate) {
        try {
            validator.validate(service, certificate);
            return true;
        } catch (PeppolSecurityException e) {
            return false;
        }
    }

    public static String extractMicalg(MimeMessage message) throws OxalisAs2Exception {
        try {
            ContentType contentType = new ContentType(message.getContentType());
            String micalg = contentType.getParameter("micalg");
            if (micalg == null)
                throw new OxalisAs2Exception("Parameter 'micalg' is not provided.");

            return micalg;
        } catch (MessagingException e) {
            throw new OxalisAs2Exception("Unable to fetch content type.", e);
        }
    }
}
