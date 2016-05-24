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
import eu.peppol.as2.As2Header;
import eu.peppol.as2.InboundMessageReceiver;
import eu.peppol.as2.MdnData;
import eu.peppol.as2.MimeMessageHelper;
import eu.peppol.as2.servlet.ResponseData;
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
import java.util.Enumeration;

/**
 * @author steinar
 * @author thore
 */
@Singleton
public class AS2Servlet extends HttpServlet {

    public static final Logger log = LoggerFactory.getLogger(AS2Servlet.class);

    @Inject
    private InboundMessageReceiver inboundMessageReceiver;

    /**
     */
    @Override
    public void init(ServletConfig servletConfig) {

    }


    /**
     * Receives the POST'ed AS2 message.
     * <p>
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
            ResponseData responseData = inboundMessageReceiver.receive(headers, request.getInputStream());

            // Returns the MDN
            writeResponseMessageWithMdn(request, response, responseData);

        } catch (Exception e) {
            // Unexpected internal error, cannot proceed, return HTTP 500 and partly MDN to indicating the problem
            log.error("Internal error occured: " + e.getMessage(), e);
            log.error("Attempting to return MDN with explanatory message and HTTP 500 status");
            writeFailureWithExplanation(request, response, e);
        }

    }


    /**
     * Emits the Http response based upon the ResponseData object returned by the InboundMessageReceiver
     *
     * @param request
     * @param response
     * @param responseData
     * @throws IOException
     */
    void writeResponseMessageWithMdn(HttpServletRequest request, HttpServletResponse response, ResponseData responseData) throws IOException {

        try {
            // Adds MDN headers to http response and modifies the mime message
            setHeadersForMDN(response, responseData);

            // Sets the http status code, should normally be 200. If something went wrong in the processing, the MDN will contain the error
            response.setStatus(responseData.getHttpStatus());
            responseData.getSignedMdn().writeTo(response.getOutputStream());
            response.getOutputStream().flush();

            if (responseData.getHttpStatus() == HttpServletResponse.SC_OK) {
                log.debug("AS2 message processed: OK");
            } else {
                log.warn("AS2 message processed: ERROR");
            }

            log.debug("Served request, status=" + responseData.getHttpStatus());

            // Dumps the headers in the request for debug purposes
            logRequestHeaders(request);

            log.debug("\n" + MimeMessageHelper.toString(responseData.getSignedMdn()));
            log.debug("\n------------- INFO ON PROCESSED REQUEST ENDS HERE -----------");
        } catch (MessagingException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Severe error during write of MDN to http response:" + e.getMessage());
        }
    }

    /** Dumps the http request headers of the request */
    private void logRequestHeaders(HttpServletRequest request) {
        log.debug("Request headers:");
        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
            String headerName = headerNames.nextElement();
            for (Enumeration<String> values = request.getHeaders(headerName); values.hasMoreElements(); ) {
                String value = values.nextElement();
                log.debug(headerName + ": " + value);
            }
        }
    }


    /** Modifies the Http response header and the Mime message by moving some of the headers from the signed MDN
     * to the http response headers.
     *
     * @param response the http response
     * @param responseData the ResponseData instance returned by the InboundMessageReceiver
     * @throws MessagingException
     */
    void setHeadersForMDN(HttpServletResponse response, ResponseData responseData) throws MessagingException {
        // adds http headers with content type etc
        MimeMessage mimeMessage = responseData.getSignedMdn();
        MdnData mdnData = responseData.getMdnData();

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

        response.setDateHeader("Date", System.currentTimeMillis());
    }

    /** If the AS2 message processing failed with an exception, we have an internal error and act accordingly */
    void writeFailureWithExplanation(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            log.error("Internal error: " + e.getMessage(), e);

            logRequestHeaders(request);

            response.getWriter().write("INTERNAL ERROR!!");
            log.error("\n---------- REQUEST FAILURE INFORMATION ENDS HERE --------------"); // Being helpful to those who must read the error logs
    }

    /**
     * Copies the http request headers into an InternetHeaders object, which is more usefull when working with MIME.
     *
     * @param request
     * @return
     */
    private InternetHeaders copyHttpHeadersIntoMap(HttpServletRequest request) {
        InternetHeaders internetHeaders = new InternetHeaders();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();

            // Never mind header names can have several values
            String value = request.getHeader(name);
            internetHeaders.addHeader(name, value);
            log.debug("HTTP-Header : " + name + "=" + value);
        }
        return internetHeaders;
    }

    /** Allows for simple http GET requests */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("HTTP GET not supported");
        response.setStatus(200);
        response.getOutputStream().println("Hello AS2 world\n");
    }

    /*
    // Uncomment to debug incoming requests.
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
