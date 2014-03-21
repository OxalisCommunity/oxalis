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

package eu.peppol.document;

import eu.peppol.PeppolStandardBusinessHeader;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 * @author thore
 */
public class SbdhWrapperTest {

    @Test
    public void testWrapWithHeaders() throws Exception {

        // parse headers from an existing file
        URL resource = ParseSbdhTest.class.getClassLoader().getResource("peppol-bis-invoice-sbdh.xml");
        assertNotNull(resource);
        File file = new File(resource.toURI());
        assertTrue(file.isFile() && file.canRead());
        PeppolStandardBusinessHeader headers = new SbdhParser().parse(new FileInputStream(file));

        // wrap a new document in sbdh using the same headers
        InputStream resourceAsStream = SbdhWrapperTest.class.getClassLoader().getResourceAsStream("ehf-invoice-no-sbdh.xml");
        assertNotNull(resourceAsStream);
        SbdhWrapper sbdhWrapper = new SbdhWrapper();
        byte[] wrap = sbdhWrapper.wrap(resourceAsStream, headers);

        // just print the wrapped document for visual debugging
        // String s = new String(wrap, "UTF-8");
        // System.out.println(s);

        // validate that headers from result document matches the original
        PeppolStandardBusinessHeader resultHeaders = new SbdhParser().parse(new ByteArrayInputStream(wrap));
        assertEquals(resultHeaders.getSenderId(), headers.getSenderId());
        assertEquals(resultHeaders.getRecipientId(), headers.getRecipientId());
        assertEquals(resultHeaders.getDocumentTypeIdentifier(), headers.getDocumentTypeIdentifier());
        assertEquals(resultHeaders.getProfileTypeIdentifier(), headers.getProfileTypeIdentifier());
        // assertEquals(resultHeaders.getMessageId(), headers.getMessageId());
        // assertEquals(resultHeaders.getCreationDateAndTime(), headers.getCreationDateAndTime());

    }

}
