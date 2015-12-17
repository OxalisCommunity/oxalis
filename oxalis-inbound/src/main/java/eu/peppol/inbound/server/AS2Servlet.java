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

package eu.peppol.inbound.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.peppol.as2.*;
import eu.peppol.as2.evidence.As2TransmissionEvidenceFactory;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.TransmissionEvidence;
import eu.peppol.security.KeystoreManager;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.xsd.ticc.receipt._1.TransmissionRole;
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

    @Inject
    private InboundMessageReceiver inboundMessageReceiver;

    @Inject
    private MessageRepository messageRepository;

    @Inject
    private RawStatisticsRepository rawStatisticsRepository;

    @Inject
    As2TransmissionEvidenceFactory as2TransmissionEvidenceFactory;

    @Inject
    KeystoreManager keystoreManager;

    private AccessPointIdentifier ourAccessPointIdentifier;

    /**
     * Loads our X509 PEPPOL certificate togheter with our private key and initializes
     * a MdnMimeMessageFactory instance.
     */
    @Override
    public void init(ServletConfig servletConfig) {

        // Gives us access to BouncyCastle
        Security.addProvider(new BouncyCastleProvider());

        X509Certificate ourCertificate = keystoreManager.getOurCertificate();
        PrivateKey ourPrivateKey = keystoreManager.getOurPrivateKey();

        // FIXME: should be provided in AS2CommonTransmissonModule
        mdnMimeMessageFactory = new MdnMimeMessageFactory(ourCertificate, ourPrivateKey);

        // fetch the CN of our certificate
        ourAccessPointIdentifier = AccessPointIdentifier.valueOf(keystoreManager.getOurCommonName());
    }


    /**
     * Receives the POST'ed AS2 message.
     *
     * Important to note that the HTTP headers contains the MIME headers for the payload.
     * Since the the request can only be read once, using getReader()/getInputStream()
     */
    protected void doPost(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        InternetHeaders headers = copyHttpHeadersIntoMap(request);

        // Receives the data, validates the headers, signature etc., invokes the persistence handler
        // and finally returns the MdnData to be sent back to the caller
        try {

            // Performs the actual reception of the message by parsing the HTTP POST request
            // persisting the payload etc.
            As2ReceiptData as2ReceiptData = inboundMessageReceiver.receive(headers, request.getInputStream(), messageRepository, rawStatisticsRepository, ourAccessPointIdentifier);

            // Creates the S/MIME message to be returned to the sender
            MimeMessage mimeMessage = mdnMimeMessageFactory.createSignedMdn(as2ReceiptData.getMdnData(), headers);

            // Creates the signed generic transport level receipt (evidence) to be stored locally
            TransmissionEvidence transmissionEvidence = as2TransmissionEvidenceFactory.createRemWithMdnEvidence(as2ReceiptData, mimeMessage, TransmissionRole.C_3);


            // Return a positive MDN
            writeMimeMessageWithPositiveResponse(response, as2ReceiptData.getMdnData(), mimeMessage);

            // messageRepository.saveTransportReceipt(transmissionEvidence);

        } catch (ErrorWithMdnException e) {
            // Reception of AS2 message failed, send back a MDN indicating failure (always use HTTP 200 for MDN)
            log.warn("AS2 reception error: " + e.getMessage(), e);
            log.warn("Returning negative MDN with explanatory message");
            MdnData mdnData = e.getMdnData();
            MimeMessage mimeMessage = mdnMimeMessageFactory.createSignedMdn(mdnData, headers);
            writeMimeMessageWithNegativeMdn(response, e, mimeMessage, mdnData);
        } catch (Exception e) {
            // Unexpected internal error, cannot proceed, return HTTP 500 and partly MDN to indicating the problem
            log.error("Internal error occured: " + e.getMessage(), e);
            log.error("Attempting to return MDN with explanatory message and HTTP 500 status");
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(headers, null, e.getMessage());
            MimeMessage mimeMessage = mdnMimeMessageFactory.createSignedMdn(mdnData, headers);
            writeFailureWithExplanation(response, e, mimeMessage, mdnData);
        }

    }

    void writeMimeMessageWithPositiveResponse(HttpServletResponse response, MdnData mdnData, MimeMessage mimeMessage)  throws IOException {

        try {
            // Adds MDN headers to http response and modifies the mime message
            setHeadersForMDN(response, mdnData, mimeMessage);
            response.setStatus(HttpServletResponse.SC_OK);
            mimeMessage.writeTo(response.getOutputStream());
            response.getOutputStream().flush();

            log.debug("Served request, status=OK:\n" + MimeMessageHelper.toString(mimeMessage));
            log.debug("------------- INFO ON PROCESSED REQUEST ENDS HERE -----------");
        } catch (MessagingException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Severe error during write of MDN " + e.getMessage());
        }
    }

    void setHeadersForMDN(HttpServletResponse response, MdnData mdnData, MimeMessage mimeMessage) throws MessagingException {
        // adds http headers with content type etc
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
        log.debug("HTTP GET not supported");
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
