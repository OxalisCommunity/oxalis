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

package no.difi.oxalis.sniffer.sbdh;

import no.difi.oxalis.commons.sbdh.SbdhParser;
import no.difi.vefa.peppol.common.model.Header;
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

        // parse header from an existing file
        URL resource = ParseSbdhTest.class.getClassLoader().getResource("peppol-bis-invoice-sbdh.xml");
        assertNotNull(resource);
        File file = new File(resource.toURI());
        assertTrue(file.isFile() && file.canRead());
        Header header = SbdhParser.parse(new FileInputStream(file));

        // wrap a new document in sbdh using the same header
        InputStream resourceAsStream = SbdhWrapperTest.class.getClassLoader().getResourceAsStream("ehf-invoice-no-sbdh.xml");
        assertNotNull(resourceAsStream);
        SbdhWrapper sbdhWrapper = new SbdhWrapper();
        byte[] wrap = sbdhWrapper.wrap(resourceAsStream, header);

        // just print the wrapped document for visual debugging
        // String s = new String(wrap, "UTF-8");
        // System.out.println(s);

        // validate that header from result document matches the original
        Header resultHeaders = SbdhParser.parse(new ByteArrayInputStream(wrap));
        assertEquals(resultHeaders.getSender(), header.getSender());
        assertEquals(resultHeaders.getReceiver(), header.getReceiver());
        assertEquals(resultHeaders.getDocumentType(), header.getDocumentType());
        assertEquals(resultHeaders.getProcess(), header.getProcess());
        // assertEquals(resultHeaders.getInstanceId(), header.getInstanceId());
        // assertEquals(resultHeaders.getCreationDateAndTime(), header.getCreationDateAndTime());

    }

}
