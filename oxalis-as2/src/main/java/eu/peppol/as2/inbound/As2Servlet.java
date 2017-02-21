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
import eu.peppol.as2.code.MdnHeader;
import eu.peppol.as2.lang.OxalisAs2InboundException;
import eu.peppol.as2.util.MdnBuilder;
import eu.peppol.as2.util.MimeMessageHelper;
import eu.peppol.as2.util.SMimeMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * @author steinar
 * @author thore
 * @author erlend
 */
@Singleton
class As2Servlet extends HttpServlet {

    public static final Logger LOGGER = LoggerFactory.getLogger(As2Servlet.class);

    private final Provider<As2InboundHandler> inboundHandlerProvider;

    private final SMimeMessageFactory sMimeMessageFactory;

    private final Tracer tracer;

    @Inject
    public As2Servlet(Provider<As2InboundHandler> inboundHandlerProvider, SMimeMessageFactory sMimeMessageFactory,
                      Tracer tracer) {
        this.inboundHandlerProvider = inboundHandlerProvider;
        this.sMimeMessageFactory = sMimeMessageFactory;
        this.tracer = tracer;
    }

    /**
     * Receives the POST'ed AS2 message.
     * <p>
     * Important to note that the HTTP headers contains the MIME headers for the payload.
     * Since the the request can only be read once, using getReader()/getInputStream()
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        Span root = tracer.newTrace().name("as2servlet.post").start();
        root.tag("message-id", request.getHeader("message-id"));

        MDC.put("message-id", request.getHeader("message-id"));

        LOGGER.debug("Receiving HTTP POST request");

        // Read all headers
        InternetHeaders headers = copyHttpHeadersIntoMap(request);

        // Receives the data, validates the headers, signature etc., invokes the persistence handler
        // and finally returns the MdnData to be sent back to the caller
        try {
            // Read MIME message
            MimeMessage mimeMessage = MimeMessageHelper
                    .createMimeMessageAssistedByHeaders(request.getInputStream(), headers);

            try {
                // Performs the actual reception of the message by parsing the HTTP POST request
                // persisting the payload etc.

                Span span = tracer.newChild(root.context()).name("as2message").start();
                MimeMessage mdn = inboundHandlerProvider.get().receive(headers, mimeMessage);
                span.finish();

                // Returns the MDN
                span = tracer.newChild(root.context()).name("mdn").start();
                writeMdn(response, mdn, HttpServletResponse.SC_OK);
                span.finish();

            } catch (OxalisAs2InboundException e) {
                // Begin builder
                MdnBuilder mdnBuilder = MdnBuilder.newInstance(mimeMessage);

                // Original Message-Id
                mdnBuilder.addHeader(MdnHeader.ORIGINAL_MESSAGE_ID, headers.getHeader(As2Header.MESSAGE_ID)[0]);

                // Disposition from exception
                mdnBuilder.addHeader(MdnHeader.DISPOSITION, e.getDisposition());
                mdnBuilder.addText("Error", e.getMessage());

                // Build and add headers
                MimeMessage mdn = sMimeMessageFactory.createSignedMimeMessage(mdnBuilder.build());
                mdn.setHeader(As2Header.AS2_VERSION, As2Header.VERSION);
                mdn.setHeader(As2Header.AS2_FROM, headers.getHeader(As2Header.AS2_TO)[0]);
                mdn.setHeader(As2Header.AS2_TO, headers.getHeader(As2Header.AS2_FROM)[0]);

                writeMdn(response, mdn, HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            root.tag("exception", String.valueOf(e.getMessage()));

            // Unexpected internal error, cannot proceed, return HTTP 500 and partly MDN to indicating the problem
            LOGGER.error("Internal error occured: {}", e.getMessage(), e);
            LOGGER.error("Attempting to return MDN with explanatory message and HTTP 500 status");
            writeFailureWithExplanation(request, response, e);
        }

        MDC.clear();
        root.finish();
    }

    protected void writeMdn(HttpServletResponse response, MimeMessage mdn, int status)
            throws MessagingException, IOException {
        // Set HTTP status.
        response.setStatus(status);

        // Add headers and collect header names.
        String[] headers = Collections.list((Enumeration<Header>) mdn.getAllHeaders()).stream()
                .peek(h -> response.setHeader(h.getName(), h.getValue()))
                .map(Header::getName)
                .toArray(String[]::new);

        // Write MDN to response without header names.
        mdn.writeTo(response.getOutputStream(), headers);
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
