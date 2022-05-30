/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package network.oxalis.as2.util;

import org.testng.annotations.Test;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 * Date: 13.11.13
 * Time: 10:30
 */
public class MdnMimeMessageInspectorTest {

    public static final String OPENAS2_MDN_TXT = "openas2-mdn.txt";

    public static final String IBX_MDN_BASE64 = "real-mdn-examples/ibx-mdn-base64.txt";

    @Test
    public void parseOpenAS2MDN() throws Exception {

        InputStream resourceAsStream = MdnMimeMessageInspectorTest.class.getClassLoader().getResourceAsStream(OPENAS2_MDN_TXT);
        assertNotNull(resourceAsStream, "Unable to find " + OPENAS2_MDN_TXT + " in class path");

        MimeMessage mimeMessage = MimeMessageHelper.parse(resourceAsStream);
        MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
        String plainText = mdnMimeMessageInspector.getPlainTextPartAsText();
        assertNotNull(plainText);

    }

    @Test
    public void parseOpenAS2MDNFields() throws Exception {

        InputStream resourceAsStream = MdnMimeMessageInspectorTest.class.getClassLoader().getResourceAsStream(OPENAS2_MDN_TXT);
        assertNotNull(resourceAsStream, "Unable to find " + OPENAS2_MDN_TXT + " in class path");

        MimeMessage mimeMessage = MimeMessageHelper.parse(resourceAsStream);
        MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
        Map<String, String> fields = mdnMimeMessageInspector.getMdnFields();
        assertEquals(fields.get("Original-Recipient"), "rfc822; OpenAS2A");
        assertEquals(fields.get("Final-Recipient"), "rfc822; OpenAS2A");
        assertEquals(fields.get("Original-Message-ID"), "42");
        assertEquals(fields.get("Received-Content-MIC"), "Fp67Ews9SJa5pKGXVl07dBuVW4I=, sha1");
    }

    @Test
    public void parseBase64EncodedMDN() throws Exception {

        InputStream resourceAsStream = MdnMimeMessageInspectorTest.class.getClassLoader().getResourceAsStream(IBX_MDN_BASE64);
        assertNotNull(resourceAsStream, "Unable to find " + IBX_MDN_BASE64 + " in class path");

        MimeMessage mimeMessage = MimeMessageHelper.parse(resourceAsStream);
        MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
        Map<String, String> fields = mdnMimeMessageInspector.getMdnFields();
        assertEquals(fields.size(), 6);
        assertEquals(fields.get("Reporting-UA"), "Oxalis");
        assertEquals(fields.get("Disposition"), "automatic-action/MDN-sent-automatically; processed");
        assertEquals(fields.get("Original-Recipient"), "rfc822; APP_1000000030");
        assertEquals(fields.get("Final-Recipient"), "rfc822; APP_1000000030");
        assertEquals(fields.get("Original-Message-ID"), "19a9099c-c553-4ffa-9c60-de2fcfa2922f");
        assertEquals(fields.get("Received-Content-MIC"), "VZOW8aRv9e8uEQEdGRdxwcOYH1g=, sha1");

    }

}
