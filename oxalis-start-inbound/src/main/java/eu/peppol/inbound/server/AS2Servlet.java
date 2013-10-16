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

import eu.peppol.as2.*;
import eu.peppol.inbound.as2.As2MessageFactory;
import org.bouncycastle.mail.smime.SMIMESignedParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author steinar
 *         Date: 20.06.13
 *         Time: 00:32
 */
public class AS2Servlet extends HttpServlet {

    public static final Logger log = LoggerFactory.getLogger(AS2Servlet.class);

    protected void doPost(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        try {
            // Parses the incoming HTTP POST into an AS2 message
            As2Message as2Message = As2MessageFactory.createAs2MessageFrom(request);

            // Save everything for logging purposes?

            // Validates the message headers according to the PEPPOL rules
            MimeMessageInspector mimeMessageInspector = As2MessageInspector.validate(as2Message);

            // Saves the payload

            // Creates the MDN if so requested

            // TODO: How should we handle asynch. MDN requests?

            log.debug("Received MimeMessage: " + as2Message.getMimeMessage().getContentType());

            // Should always be signed
            if (as2Message.getMimeMessage().isMimeType("multipart/signed")) {
                // Parses the multipart signed into the content and the signature...
                SMIMESignedParser smimeSignedParser = new SMIMESignedParser((MimeMultipart) as2Message.getMimeMessage().getContent());

                // Dumps the payload into a file.
                MimeBodyPart content = smimeSignedParser.getContent();
                FileOutputStream fileOutputStream = new FileOutputStream("/tmp/as2dump.xml");
                InputStream inputStream = content.getInputStream();
                int i = 0;
                while ((i = inputStream.read()) != -1) {
                    fileOutputStream.write(i);
                }
                fileOutputStream.close();

            }
        } catch (InvalidAs2MessageException e) {
            log.error("Error in the AS2 Message input " + e.getMessage(), e);
            // TODO: emit an MDN and set the HTTP return code etc.
            MdnData.Builder builder = new MdnData.Builder();
            builder.date(new Date());


            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            response.setContentType("text/plain");
            response.getWriter().write("Holly miracle!\n");
        } catch (Exception e) {

            // Creates MDN with an error message and returns it
            log.error("Unable to parse SMIME message " + e, e);
            throw new IllegalStateException("Unable to parse SMIME signed message" + e.getMessage(), e);
        }
//        dumpData(request);
    }

    private Map copyHttpHeadersIntoMap(HttpServletRequest request) {

        HashMap<String, String> headers = new HashMap<String, String>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            String value = request.getHeader(name);
            headers.put(name, value);
        }


        return headers;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    private void dumpData(HttpServletRequest request) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/as2dump.txt");
        ServletInputStream inputStream = request.getInputStream();
        int i = 0;
        while ((i=inputStream.read()) != -1){
            fileOutputStream.write(i);
        }
        fileOutputStream.close();
    }
}
