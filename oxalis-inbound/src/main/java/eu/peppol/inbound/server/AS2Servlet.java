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
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;

/**
 *
 * @author steinar
 *         Date: 20.06.13
 *         Time: 00:32
 */
@Singleton
public class AS2Servlet extends HttpServlet {

    // TODO: implement Guice initialization of AS2Servlet to inject dependencies.

    public static final Logger log = LoggerFactory.getLogger(AS2Servlet.class);

    private MdnMimeMessageFactory mdnMimeMessageFactory;
    private InboundMessageReceiver inboundMessageReceiver;
    private RawStatisticsRepository rawStatisticsRepository;
    private MessageRepository messageRepository;


    /**
     * Loads our X509 PEPPOL certificate togheter with our private key and initializes
     * a MdnMimeMessageFactory instance.
     *
     * @param servletConfig
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
    }


    /**
     * Receives the POST'ed AS2 message.
     *
     * Important to note that the HTTP headers contains the MIME headers for the payload.
     * Since the the request can only be read once, using getReader()/getInputStream() and
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        InternetHeaders headers = copyHttpHeadersIntoMap(request);

        // Receives the data, validates the headers, signature etc., invokes the persistence handler
        // and finally returns the MdnData to be sent back to the caller
        try {

            // Performs the actual reception of the message by parsing the HTTP POST request
            MdnData mdnData = inboundMessageReceiver.receive(headers, request.getInputStream(), messageRepository);

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
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Severe error during write of MDN " + e.getMessage());
            }

        } catch (ErrorWithMdnException e) {
            // Reception of AS2 message failed, send back a MDN indicating failure (always use HTTP 200 for MDN)
            response.setStatus(HttpServletResponse.SC_OK);
            MimeMessage mimeMessage = mdnMimeMessageFactory.createMdn(e.getMdnData(), headers);
            writeMimeMessageWithMdn(response, e, mimeMessage);
        } catch (Exception e) {
            // Unexpected internal error, return MDN indicating the problem (always use HTTP 200 for MDN)
            response.setStatus(HttpServletResponse.SC_OK);
            log.error("Internal error occured: " + e.getMessage(), e);
            log.error("Attempting to return MDN with explanatory message");
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(headers, e.getMessage());
            MimeMessage mimeMessage = mdnMimeMessageFactory.createMdn(mdnData, headers);
            writeMimeMessageWithMdn(response, e, mimeMessage);
        }

    }

    void setHeadersForMDN(HttpServletResponse response, MdnData mdnData, MimeMessage mimeMessage) throws MessagingException {
        response.setHeader("Message-ID", mimeMessage.getHeader("Message-ID")[0]);
        response.setHeader("MIME-Version", "1.0");
        response.setHeader("Content-Type", mimeMessage.getContentType());
        response.setHeader("AS2-To", mdnData.getAs2To());
        response.setHeader("AS2-From", mdnData.getAs2From());
        response.setHeader(As2Header.AS2_VERSION.getHttpHeaderName(), As2Header.VERSION);
        response.setHeader(As2Header.SERVER.getHttpHeaderName(), "Oxalis");
        response.setHeader("Subject", mdnData.getSubject());

        mimeMessage.removeHeader("Message-ID");
        mimeMessage.removeHeader("MIME-Version");

        String date = As2DateUtil.format(new Date());
        response.setHeader("Date", date);
    }

    private void writeMimeMessageWithMdn(HttpServletResponse response, Exception e, MimeMessage mimeMessage) throws IOException {
        try {
            mimeMessage.writeTo(response.getOutputStream());

            log.error("Returned MDN with failure: " + MimeMessageHelper.toString(mimeMessage), e);
            log.error("---------- REQUEST ERROR INFORMATION ENDS HERE --------------"); // Being helpful to those who must read the error logs
        } catch (MessagingException e1) {
            String msg = "Unable to return MDN with failure to sender; " + e1.getMessage();
            log.error(msg);
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

    private void dumpData(HttpServletRequest request) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/as2dump.txt");
        ServletInputStream inputStream = request.getInputStream();
        int i = 0;
        while ((i = inputStream.read()) != -1) {
            fileOutputStream.write(i);
        }
        fileOutputStream.close();
    }
}
