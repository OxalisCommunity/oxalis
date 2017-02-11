/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.peppol.as2.inbound;

import brave.Span;
import brave.Tracer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.peppol.as2.code.As2Header;
import eu.peppol.as2.model.MdnData;
import eu.peppol.as2.util.MimeMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * @author steinar
 * @author thore
 */
@Singleton
class As2Servlet extends HttpServlet {

    public static final Logger LOGGER = LoggerFactory.getLogger(As2Servlet.class);

    private Provider<As2InboundHandler> inboundMessageReceiver;

    private Tracer tracer;

    @Inject
    public As2Servlet(Provider<As2InboundHandler> inboundMessageReceiver, Tracer tracer) {
        this.inboundMessageReceiver = inboundMessageReceiver;
        this.tracer = tracer;
    }

    /**
     */
    @Override
    public void init(ServletConfig servletConfig) {
        // No action.
    }

    /**
     * Receives the POST'ed AS2 message.
     * <p>
     * Important to note that the HTTP headers contains the MIME headers for the payload.
     * Since the the request can only be read once, using getReader()/getInputStream()
     */
    @Override
    protected void doPost(final HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Span root = tracer.newTrace().name("as2servlet.post").start();
        root.tag("message-id", request.getHeader("message-id"));

        MDC.put("message-id", request.getHeader("message-id"));

        LOGGER.debug("Receiving HTTP POST request");
        InternetHeaders headers = copyHttpHeadersIntoMap(request);

        // Receives the data, validates the headers, signature etc., invokes the persistence handler
        // and finally returns the MdnData to be sent back to the caller
        try {
            // Performs the actual reception of the message by parsing the HTTP POST request
            // persisting the payload etc.

            Span span = tracer.newChild(root.context()).name("as2message").start();
            ResponseData responseData = inboundMessageReceiver.get().receive(headers, request.getInputStream());
            span.finish();

            // Returns the MDN
            span = tracer.newChild(root.context()).name("mdn").start();
            writeResponseMessageWithMdn(request, response, responseData);
            span.finish();
        } catch (Exception e) {
            root.tag("exception", e.getMessage());

            // Unexpected internal error, cannot proceed, return HTTP 500 and partly MDN to indicating the problem
            LOGGER.error("Internal error occured: " + e.getMessage(), e);
            LOGGER.error("Attempting to return MDN with explanatory message and HTTP 500 status");
            writeFailureWithExplanation(request, response, e);
        }

        MDC.clear();
        root.finish();

    }

    /**
     * Emits the Http response based upon the ResponseData object returned by the As2InboundHandler
     */
    void writeResponseMessageWithMdn(HttpServletRequest request, HttpServletResponse response,
                                     ResponseData responseData) throws IOException {
        try {

            // Adds MDN headers to http response and modifies the mime message
            setHeadersForMDN(response, responseData);

            // Sets the http status code, should normally be 200. If something went wrong in the processing,
            // the MDN will contain the error
            response.setStatus(responseData.getHttpStatus());
            responseData.getSignedMdn().writeTo(response.getOutputStream());
            response.getOutputStream().flush();

            if (responseData.getHttpStatus() == HttpServletResponse.SC_OK) {
                LOGGER.debug("AS2 message processed: OK");
            } else {
                LOGGER.warn("AS2 message processed: ERROR");
            }

            LOGGER.debug("Served request, status=" + responseData.getHttpStatus());

            // Dumps the headers in the request for debug purposes
            logRequestHeaders(request);

            LOGGER.debug("\n" + MimeMessageHelper.toString(responseData.getSignedMdn()));
        } catch (MessagingException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Severe error during write of MDN to http response:" + e.getMessage());
        }
    }

    /**
     * Dumps the http request headers of the request
     */
    private void logRequestHeaders(HttpServletRequest request) {
        LOGGER.debug("Request headers:");
        Collections.list(request.getHeaderNames())
                .forEach(name -> LOGGER.debug("=> {}: {}", name, request.getHeader(name)));
    }


    /**
     * Modifies the Http response header and the Mime message by moving some of the headers from the signed MDN
     * to the http response headers.
     *
     * @param response     the http response
     * @param responseData the ResponseData instance returned by the As2InboundHandler
     */
    void setHeadersForMDN(HttpServletResponse response, ResponseData responseData) throws MessagingException {
        // adds http headers with content type etc
        MimeMessage mimeMessage = responseData.getSignedMdn();
        MdnData mdnData = responseData.getMdnData();

        response.setHeader(As2Header.MESSAGE_ID, mimeMessage.getHeader("Message-ID")[0]);
        response.setHeader("MIME-Version", "1.0");
        response.setHeader("Content-Type", mimeMessage.getContentType());
        response.setHeader(As2Header.AS2_TO, mdnData.getAs2To());
        response.setHeader(As2Header.AS2_FROM, mdnData.getAs2From());
        response.setHeader(As2Header.AS2_VERSION, As2Header.VERSION);
        response.setHeader(As2Header.SERVER, "Oxalis");
        response.setHeader(As2Header.SUBJECT, mdnData.getSubject());

        // remove headers from the outer mime message (they are present in the http headers)
        mimeMessage.removeHeader(As2Header.MESSAGE_ID);
        mimeMessage.removeHeader("MIME-Version");
        mimeMessage.removeHeader("Content-Type");

        response.setDateHeader("Date", System.currentTimeMillis());
    }

    /**
     * If the AS2 message processing failed with an exception, we have an internal error and act accordingly
     */
    void writeFailureWithExplanation(HttpServletRequest request, HttpServletResponse response, Exception e)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        LOGGER.error("Internal error: " + e.getMessage(), e);

        logRequestHeaders(request);

        response.getWriter().write("INTERNAL ERROR!!");
        // Being helpful to those who must read the error logs
        LOGGER.error("\n---------- REQUEST FAILURE INFORMATION ENDS HERE --------------");
    }

    /**
     * Copies the http request headers into an InternetHeaders object, which is more usefull when working with MIME.
     */
    private InternetHeaders copyHttpHeadersIntoMap(HttpServletRequest request) {
        InternetHeaders internetHeaders = new InternetHeaders();
        Collections.list(request.getHeaderNames())
                .forEach(name -> internetHeaders.addHeader(name, request.getHeader(name)));
        return internetHeaders;
    }

    /**
     * Allows for simple http GET requests
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getOutputStream().println("Hello AS2 world\n");
    }
}
