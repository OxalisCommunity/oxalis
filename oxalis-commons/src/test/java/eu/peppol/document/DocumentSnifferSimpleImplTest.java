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

import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 06.11.13
 *         Time: 16:13
 */
public class DocumentSnifferSimpleImplTest {

    @Test
    public void sniffDocument() throws Exception {

        InputStream resourceAsStream = NoSbdh2PeppolHeaderParserTest.class.getClassLoader().getResourceAsStream("ehf-invoice-no-sbdh.xml");
        assertNotNull(resourceAsStream);

        if (resourceAsStream.markSupported()) {
            resourceAsStream.mark(Integer.MAX_VALUE);
        }

        DocumentSniffer documentSniffer = new DocumentSnifferSimpleImpl(resourceAsStream);
        assertFalse(documentSniffer.isSbdhDetected());
        resourceAsStream.reset();
        resourceAsStream.close();

        InputStream sbdhStream = NoSbdh2PeppolHeaderParserTest.class.getClassLoader().getResourceAsStream("peppol-bis-invoice-sbdh.xml");
        assertNotNull(sbdhStream);
        documentSniffer = new DocumentSnifferSimpleImpl(sbdhStream);
        assertTrue(documentSniffer.isSbdhDetected());
        assertTrue(sbdhStream.markSupported());

    }

}
