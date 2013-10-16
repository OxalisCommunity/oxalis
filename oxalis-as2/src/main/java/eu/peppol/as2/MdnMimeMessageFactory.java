package eu.peppol.as2;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

/**
 * Creates S/MIME MDN messages.
 * <pre>
 Date: Wed, 09 Oct 2013 20:56:21 +0200
 From: OpenAS2 A email
 Message-ID: <OPENAS2-09102013205621+0200-4452@OpenAS2A_OpenAS2B>
 Subject: Your Requested MDN Response
 Mime-Version: 1.0
 Content-Type: multipart/signed; protocol="application/pkcs7-signature"; micalg=sha1; boundary="----=_Part_5_985951695.1381344981855"
 AS2-To: OpenAS2B
 AS2-From: OpenAS2A
 AS2-Version: 1.1
 Server: ph-OpenAS2 v1.0
 Content-Length: 2151

 ------=_Part_5_985951695.1381344981855
 Content-Type: multipart/report; report-type=disposition-notification;
 boundary="----=_Part_3_621450213.1381344981854"

 ------=_Part_3_621450213.1381344981854
 Content-Type: text/plain
 Content-Transfer-Encoding: 7bit

 The message sent to Recipient OpenAS2A on Wed Oct  9 20:56:20 CEST 2013 with Subject PEPPOL message has been received, the EDI Interchange was successfully decrypted and it's integrity was verified. In addition, the sender of the message, Sender OpenAS2B at Location /127.0.0.1 was authenticated as the originator of the message. There is no guarantee however that the EDI Interchange was syntactically correct, or was received by the EDI application/translator.

 ------=_Part_3_621450213.1381344981854
 Content-Type: message/disposition-notification
 Content-Transfer-Encoding: 7bit

 Reporting-UA: ph-OpenAS2 v1.0@/127.0.0.1:10080
 Original-Recipient: rfc822; OpenAS2A
 Final-Recipient: rfc822; OpenAS2A
 Original-Message-ID: 42
 Disposition: automatic-action/MDN-sent-automatically; processed
 Received-Content-MIC: eeWNkOTx7yJYr2EW8CR85I7QJQY=, sha1


 ------=_Part_3_621450213.1381344981854--

 ------=_Part_5_985951695.1381344981855
 Content-Type: application/pkcs7-signature; name=smime.p7s; smime-type=signed-data
 Content-Transfer-Encoding: base64
 Content-Disposition: attachment; filename="smime.p7s"
 Content-Description: S/MIME Cryptographic Signature

 MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAMYIBrjCCAaoC
 AQEwKDAgMQswCQYDVQQGEwJBVDERMA8GA1UEAwwIT3BlbkFTMkECBFGKVcEwCQYFKw4DAhoFAKBd
 MBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEwHAYJKoZIhvcNAQkFMQ8XDTEzMTAwOTE4NTYyMVow
 IwYJKoZIhvcNAQkEMRYEFHXj+eHHG1V6qS+aeGEkWk7LUoN4MA0GCSqGSIb3DQEBAQUABIIBAIBs
 Qr+nyyYi+LBN348WJhuiox5o7Z7S+qGGTooYFDoY25xtZLNbpKG+yqzNAJJCEe3U0vFKyQD8Z77J
 K8hsfbPJMPrLoGV4NxER60FrXRW2mEU8JGepe0Wrc0czVxyfO4gGSUUKLJAmZ9JnWpFY6YuZqhpg
 5EZmhZ4heHzjh1r9w/mKW8M0mewWJSqvNL2Kxw8CugWrJplmPRLvH9e9CVdLk6jhN2YDBe2aShv7
 36pztZZBskMLMMvbGCcTvnhVK9mAx36f6zliXin7PnYd26Ef538IUCGqSfOEhX2E0jwAfrfvrD4d
 gvx2CUISUR8xKONlC/vWq6fNebW/Z8YWfzUAAAAAAAA=
 ------=_Part_5_985951695.1381344981855--
 * </pre>
 *
 * @author steinar
 *         Date: 07.09.13
 *         Time: 22:00
 */
public class MdnMimeMessageFactory {

    private final X509Certificate ourCertificate;
    private final PrivateKey ourPrivateKey;

    public MdnMimeMessageFactory(X509Certificate ourCertificate, PrivateKey ourPrivateKey) {

        this.ourCertificate = ourCertificate;
        this.ourPrivateKey = ourPrivateKey;
    }


    public MimeMessage createMdn(MdnData mdnData) {
        MimeBodyPart humanReadablePart = humanReadablePart(mdnData);

        MimeBodyPart machineReadablePart = machineReadablePart(mdnData);

        MimeBodyPart mimeBodyPart = wrapHumandAndMachineReadableParts(humanReadablePart, machineReadablePart);

        MimeMessageFactory mimeMessageFactory = new MimeMessageFactory(ourPrivateKey, ourCertificate);

        MimeMessage signedMimeMessage = mimeMessageFactory.createSignedMimeMessage(mimeBodyPart);

        try {
            signedMimeMessage.addHeader("AS-To", "AP00001");
            signedMimeMessage.saveChanges();
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to set header:" + e, e);
        }

        return signedMimeMessage;
    }


    private MimeBodyPart humanReadablePart(MdnData mdnData) {
        MimeBodyPart humanReadablePart = null;
        try {
            humanReadablePart = new MimeBodyPart();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
            StringBuilder sb = new StringBuilder();
            sb.append("The message sent to Recipient ")
                    .append(mdnData.getAs2From().toString())
                    .append(" on ")
                    .append(simpleDateFormat.format(mdnData.getDate()))
                    .append(" with subject ")
                    .append(mdnData.getSubject())
                    .append(" has been received.")
            ;
            if (mdnData.getAs2Disposition().getDispositionType() == As2Disposition.DispositionType.PROCESSED){
                sb.append("It has been processed ");

                As2Disposition.DispositionModifier dispositionModifier = mdnData.getAs2Disposition().getDispositionModifier();

                // TODO: use strict typing for the disposition modifier object.
                if (dispositionModifier == null) {
                    sb.append("successfully.");
                } else if (dispositionModifier != null) {
                    if (dispositionModifier.toString().contains("warning")) {
                        sb.append("with a warning.");
                    } else if (dispositionModifier.toString().contains("error")) {
                        sb.append("with an error. Henceforth the message will NOT be delivered.");
                    } else if (dispositionModifier.toString().contains("failed")) {
                        sb.append("with a failed. Henceforth the message will NOT be delivered");
                    }
                }
            }

            humanReadablePart.setContent(sb.toString(), "text/plain");
            humanReadablePart.setHeader("Content-Type", "text/plain");
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to create bodypart for human readable part: " + e, e);
        }
        return humanReadablePart;
    }

    private MimeBodyPart machineReadablePart(MdnData mdnData) {
        MimeBodyPart machineReadablePart = null;
        try {
            machineReadablePart = new MimeBodyPart();

            InternetHeaders internetHeaders = new InternetHeaders();
            internetHeaders.addHeader("Reporting-UA", "oxalis");
            internetHeaders.addHeader("Disposition", mdnData.getAs2Disposition().toString());
            String recipient = "rfc822; " + mdnData.getAs2To().toString();
            internetHeaders.addHeader("Original-Recipient", recipient);
            internetHeaders.addHeader("Final-Recipient", recipient);


            StringBuilder stringBuilder = new StringBuilder();
            Enumeration enumeration = internetHeaders.getAllHeaderLines();
            while (enumeration.hasMoreElements()) {
                stringBuilder.append(enumeration.nextElement()).append("\r\n");
            }

            machineReadablePart.setContent(stringBuilder.toString(), "message/disposition-notification");

        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to create MimeBodyPart:" + e, e);
        }
        return machineReadablePart;
    }


    private MimeBodyPart wrapHumandAndMachineReadableParts(MimeBodyPart humanReadablePart, MimeBodyPart machineReadablePart) {
        MimeMultipart mimeMultipart = new MimeMultipart();
        try {

            mimeMultipart.addBodyPart(humanReadablePart);
            mimeMultipart.addBodyPart(machineReadablePart);
        } catch (MessagingException e) {
            throw new IllegalArgumentException("Unable to add body parts to multipart:"+ e.getMessage(), e);
        }

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        try {
            mimeMultipart.setSubType ("report; report-type=disposition-notification");
            mimeBodyPart.setContent (mimeMultipart);
            mimeBodyPart.setHeader ("Content-Type", mimeMultipart.getContentType ());

        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to create MIME body part " + e, e);
        }
        return mimeBodyPart;
    }

}
