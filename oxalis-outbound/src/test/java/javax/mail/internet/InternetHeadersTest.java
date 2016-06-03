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

package javax.mail.internet;

import eu.peppol.as2.As2Header;
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

        internetHeaders.addHeader(As2Header.AS2_TO.getHttpHeaderName(), "AP_1");
        String[] header = internetHeaders.getHeader("aS2-to");
        assertNotNull(header);
        assertEquals(header[0], "AP_1");


        internetHeaders.setHeader(As2Header.AS2_TO.getHttpHeaderName(), "AP_2");

        header = internetHeaders.getHeader(As2Header.AS2_TO.getHttpHeaderName());

        assertEquals(1, header.length);
        assertEquals(header[0], "AP_2");

    }
}
