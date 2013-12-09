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

package javax.mail.internet;

import eu.peppol.as2.As2Header;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 18.11.13
 *         Time: 15:10
 */
public class InternetHeadersTest {
    @Test
    public void createSampleInternetHeaders() throws Exception {

        ByteArrayInputStream is = new ByteArrayInputStream("\r\n".getBytes());
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
