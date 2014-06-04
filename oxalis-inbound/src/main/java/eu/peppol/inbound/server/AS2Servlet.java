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

package eu.peppol.inbound.server;

import com.google.inject.Singleton;
import eu.peppol.as2.*;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.security.KeystoreManager;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.start.persistence.MessageRepositoryFactory;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.RawStatisticsRepositoryFactoryProvider;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;

/**
 *
 * @author steinar
 * @author thore
 */
@Singleton
public class AS2Servlet extends HttpServlet {

    // TODO: implement Guice initialization of AS2Servlet to inject dependencies.

    public static final Logger log = LoggerFactory.getLogger(AS2Servlet.class);

    private MdnMimeMessageFactory mdnMimeMessageFactory;
    private InboundMessageReceiver inboundMessageReceiver;
    private MessageRepository messageRepository;
    private RawStatisticsRepository rawStatisticsRepository;
    private AccessPointIdentifier ourAccessPointIdentifier;

    /**
     * Loads our X509 PEPPOL certificate togheter with our private key and initializes
     * a MdnMimeMessageFactory instance.
     */
    @Override
    public void init(ServletConfig servletConfig) {

        KeystoreManager keystoreManager = KeystoreManager.getInstance();
        X509Certificate ourCertificate = keystoreManager.getOurCertificate();
        PrivateKey ourPrivateKey = keystoreManager.getOurPrivateKey();
        mdnMimeMessageFactory = new MdnMimeMessageFactory(ourCertificate, ourPrivateKey);

        // Gives us access to BouncyCastle
        Security.addProvider(new BouncyCastleProvider());

        // Gives us access to the Message repository holding the received messages
        messageRepository = MessageRepositoryFactory.getInstance();

        // Creates the receiver for inbound messages
        inboundMessageReceiver = new InboundMessageReceiver();

        // Locates an instance of the repository used for storage of raw statistics
        rawStatisticsRepository = RawStatisticsRepositoryFactoryProvider.getInstance().getInstanceForRawStatistics();

        // fetch the CN of our certificate
        ourAccessPointIdentifier = AccessPointIdentifier.valueOf(KeystoreManager.getInstance().getOurCommonName());
    }

    /**
     * Receives the POST'ed AS2 message.
     *
     * Important to note that the HTTP headers contains the MIME headers for the payload.
     * Since the the request can only be read once, using getReader()/getInputStream()
     */
    protected void doPost(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        /*
        https://www.rfc-editor.org/rfc/rfc2311.txt section 3.4
        There are two formats for signed messages defined for S/MIME:
        application/pkcs7-mime and SignedData, and multipart/signed. In
        general, the multipart/signed form is preferred for sending, and
        receiving agents SHOULD be able to handle both.

           Signing Using application/pkcs7-mime and SignedData

           This signing format uses the application/pkcs7-mime MIME type. The
           steps to create this format are:

             Step 1. The MIME entity is prepared according to section 3.1

             Step 2. The MIME entity and other required data is processed into a
                     PKCS #7 object of type signedData

             Step 3. The PKCS #7 object is inserted into an
                     application/pkcs7-mime MIME entity

           The smime-type parameter for messages using application/pkcs7-mime
           and SignedData is "signed-data". The file extension for this type of
           message is ".p7m".

           Creating a multipart/signed Message

             Step 1. The MIME entity to be signed is prepared according to
                     section 3.1, taking special care for clear-signing.

             Step 2. The MIME entity is presented to PKCS #7 processing in order
                     to obtain an object of type signedData with an empty
                     contentInfo field.

             Step 3. The MIME entity is inserted into the first part of a
                     multipart/signed message with no processing other than that
                     described in section 3.1.

             Step 4. Transfer encoding is applied to the detached signature and
                     it is inserted into a MIME entity of type
                     application/pkcs7-signature

             Step 5. The MIME entity of the application/pkcs7-signature is
                     inserted into the second part of the multipart/signed
                     entity

           The multipart/signed Content type has two required parameters: the
           protocol parameter and the micalg parameter.

           The protocol parameter MUST be "application/pkcs7-signature". Note
           that quotation marks are required around the protocol parameter
           because MIME requires that the "/" character in the parameter value
           MUST be quoted.

        */

        InternetHeaders headers = copyHttpHeadersIntoMap(request);

        // Receives the data, validates the headers, signature etc., invokes the persistence handler
        // and finally returns the MdnData to be sent back to the caller
        try {

            // Performs the actual reception of the message by parsing the HTTP POST request
            MdnData mdnData = inboundMessageReceiver.receive(headers, request.getInputStream(), messageRepository, rawStatisticsRepository, ourAccessPointIdentifier);

            // Creates the S/MIME message to be returned to the sender
            MimeMessage mimeMessage = mdnMimeMessageFactory.createMdn(mdnData, headers);

            // Add MDN headers to http response
            setHeadersForMDN(response, mdnData, mimeMessage);
            response.setStatus(HttpServletResponse.SC_OK);

            // Try to write the MDN mime message to http response
            try {
                mimeMessage.writeTo(response.getOutputStream());
                response.getOutputStream().flush();
                log.info("Served request, status=OK:\n" + MimeMessageHelper.toString(mimeMessage));
                log.info("------------- INFO ON PROCESSED REQUEST ENDS HERE -----------");
            } catch (MessagingException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Severe error during write of MDN " + e.getMessage());
            }

        } catch (ErrorWithMdnException e) {
            // Reception of AS2 message failed, send back a MDN indicating failure (always use HTTP 200 for MDN)
            log.warn("AS2 reception error: " + e.getMessage(), e);
            log.warn("Returning negative MDN with explanatory message");
            MdnData mdnData = e.getMdnData();
            MimeMessage mimeMessage = mdnMimeMessageFactory.createMdn(mdnData, headers);
            writeMimeMessageWithNegativeMdn(response, e, mimeMessage, mdnData);
        } catch (Exception e) {
            // Unexpected internal error, cannot proceed, return HTTP 500 and partly MDN to indicating the problem
            log.error("Internal error occured: " + e.getMessage(), e);
            log.error("Attempting to return MDN with explanatory message and HTTP 500 status");
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(headers, null, e.getMessage());
            MimeMessage mimeMessage = mdnMimeMessageFactory.createMdn(mdnData, headers);
            writeFailureWithExplanation(response, e, mimeMessage, mdnData);
        }

    }

    void setHeadersForMDN(HttpServletResponse response, MdnData mdnData, MimeMessage mimeMessage) throws MessagingException {
        // add http headers with content type etc
        response.setHeader("Message-ID", mimeMessage.getHeader("Message-ID")[0]);
        response.setHeader("MIME-Version", "1.0");
        response.setHeader("Content-Type", mimeMessage.getContentType());
        response.setHeader("AS2-To", mdnData.getAs2To());
        response.setHeader("AS2-From", mdnData.getAs2From());
        response.setHeader(As2Header.AS2_VERSION.getHttpHeaderName(), As2Header.VERSION);
        response.setHeader(As2Header.SERVER.getHttpHeaderName(), "Oxalis");
        response.setHeader("Subject", mdnData.getSubject());

        // remove headers from the outer mime message (they are present in the http headers)
        mimeMessage.removeHeader("Message-ID");
        mimeMessage.removeHeader("MIME-Version");
        mimeMessage.removeHeader("Content-Type");

        String date = As2DateUtil.format(new Date());
        response.setHeader("Date", date);
    }

    private void writeMimeMessageWithNegativeMdn(HttpServletResponse response, Exception e, MimeMessage mimeMessage, MdnData mdnData) throws IOException {
        try {
            setHeadersForMDN(response, mdnData, mimeMessage);
            response.setStatus(HttpServletResponse.SC_OK);
            mimeMessage.writeTo(response.getOutputStream());
            log.error("Returned negative MDN : " + MimeMessageHelper.toString(mimeMessage), e);
            log.error("---------- REQUEST ERROR INFORMATION ENDS HERE --------------"); // Being helpful to those who must read the error logs
        } catch (MessagingException e1) {
            String msg = "Unable to return MDN with failure to sender; " + e1.getMessage();
            log.error(msg);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(msg);
        }
    }

    private void writeFailureWithExplanation(HttpServletResponse response, Exception e, MimeMessage mimeMessage, MdnData mdnData) throws IOException {
        try {
            setHeadersForMDN(response, mdnData, mimeMessage);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mimeMessage.writeTo(response.getOutputStream());
            log.error("Returned MDN with failure: " + MimeMessageHelper.toString(mimeMessage), e);
            log.error("---------- REQUEST FAILURE INFORMATION ENDS HERE --------------"); // Being helpful to those who must read the error logs
        } catch (MessagingException e1) {
            String msg = "Unable to return failure to sender; " + e1.getMessage();
            log.error(msg);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(msg);
        }
    }

    private InternetHeaders copyHttpHeadersIntoMap(HttpServletRequest request) {
        InternetHeaders internetHeaders = new InternetHeaders();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            String value = request.getHeader(name);
            internetHeaders.addHeader(name, value);
            log.debug("HTTP-Header : " + name + "=" + value);
        }
        return internetHeaders;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("HTTP GET not supported");
        response.setStatus(200);
        response.getOutputStream().println("Hello AS2 world\n");
    }

    /*
    private void dumpData(HttpServletRequest request) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/as2dump.txt");
        ServletInputStream inputStream = request.getInputStream();
        int i;
        while ((i = inputStream.read()) != -1) {
            fileOutputStream.write(i);
        }
        fileOutputStream.close();
    }
    */

}
