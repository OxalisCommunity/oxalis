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
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 05.11.13
 *         Time: 15:14
 */
public class NoSbdh2PeppolHeaderParserTest {

    @Test
    public void sniffDocumentWithoutSBDH() throws Exception {

        InputStream resourceAsStream = NoSbdh2PeppolHeaderParserTest.class.getClassLoader().getResourceAsStream("ehf-invoice-no-sbdh.xml");
        assertNotNull(resourceAsStream);

        NoSbdhParser sniffer = new NoSbdhParser();
        PeppolStandardBusinessHeader sbdh = sniffer.parse(resourceAsStream);

        assertNotNull(sbdh.getDocumentTypeIdentifier());
        assertNotNull(sbdh.getCreationDateAndTime());
        assertNotNull(sbdh.getMessageId());
        assertNotNull(sbdh.getProfileTypeIdentifier());
        assertNotNull(sbdh.getRecipientId());
        assertNotNull(sbdh.getSenderId());

        assertEquals(sbdh.getSenderId(), new ParticipantId("9908:991974466"));
        assertEquals(sbdh.getRecipientId(), new ParticipantId("9908:889640782"));

        assertEquals(sbdh.getDocumentTypeIdentifier(), PeppolDocumentTypeIdAcronym.EHF_INVOICE.getDocumentTypeIdentifier());

    }

}
