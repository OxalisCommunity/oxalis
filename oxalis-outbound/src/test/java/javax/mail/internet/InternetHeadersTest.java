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

package javax.mail.internet;

import eu.peppol.as2.code.As2Header;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 18.11.13
 *         Time: 15:10
 */
public class InternetHeadersTest {
    @Test
    public void createSampleInternetHeaders() throws Exception {

        InternetHeaders internetHeaders = new InternetHeaders();
        assertNull(internetHeaders.getHeader("Content-Type"));

        internetHeaders.addHeader(As2Header.AS2_TO, "AP_1");
        String[] header = internetHeaders.getHeader("aS2-to");
        assertNotNull(header);
        assertEquals(header[0], "AP_1");


        internetHeaders.setHeader(As2Header.AS2_TO, "AP_2");

        header = internetHeaders.getHeader(As2Header.AS2_TO);

        assertEquals(1, header.length);
        assertEquals(header[0], "AP_2");

    }
}
