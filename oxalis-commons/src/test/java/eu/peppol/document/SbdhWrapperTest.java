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

package eu.peppol.document;

import eu.peppol.PeppolStandardBusinessHeader;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import static org.testng.Assert.*;

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
        PeppolStandardBusinessHeader headers = new Sbdh2PeppolHeaderParser().parse(new FileInputStream(file));

        // wrap a new document in sbdh using the same headers
        InputStream resourceAsStream = SbdhWrapperTest.class.getClassLoader().getResourceAsStream("ehf-invoice-no-sbdh.xml");
        assertNotNull(resourceAsStream);
        SbdhWrapper sbdhWrapper = new SbdhWrapper();
        byte[] wrap = sbdhWrapper.wrap(resourceAsStream, headers);

        // just print the wrapped document for visual debugging
        // String s = new String(wrap, "UTF-8");
        // System.out.println(s);

        // validate that headers from result document matches the original
        PeppolStandardBusinessHeader resultHeaders = new Sbdh2PeppolHeaderParser().parse(new ByteArrayInputStream(wrap));
        assertEquals(resultHeaders.getSenderId(), headers.getSenderId());
        assertEquals(resultHeaders.getRecipientId(), headers.getRecipientId());
        assertEquals(resultHeaders.getDocumentTypeIdentifier(), headers.getDocumentTypeIdentifier());
        assertEquals(resultHeaders.getProfileTypeIdentifier(), headers.getProfileTypeIdentifier());
        // assertEquals(resultHeaders.getMessageId(), headers.getMessageId());
        // assertEquals(resultHeaders.getCreationDateAndTime(), headers.getCreationDateAndTime());

    }

}
