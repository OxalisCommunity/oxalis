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

package network.oxalis.sniffer.document;

import network.oxalis.sniffer.PeppolStandardBusinessHeader;
import network.oxalis.test.identifier.PeppolDocumentTypeIdAcronym;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 05.11.13
 *         Time: 15:14
 */
public class NoSbdh2PeppolHeaderParserTest {

    @Test
    public void sniffDocumentWithoutSBDH() throws Exception {
        InputStream resourceAsStream = getClass().getResourceAsStream("/ehf-invoice-no-sbdh.xml");
        assertNotNull(resourceAsStream);

        NoSbdhParser sniffer = new NoSbdhParser();
        PeppolStandardBusinessHeader sbdh = sniffer.originalParse(resourceAsStream);

        assertNotNull(sbdh.getDocumentTypeIdentifier());
        assertNotNull(sbdh.getCreationDateAndTime());
        assertNull(sbdh.getInstanceId(), "InstanceId should not be parsed from EHF invoice with no SBDH");
        assertNotNull(sbdh.getProfileTypeIdentifier());
        assertNotNull(sbdh.getRecipientId());
        assertNotNull(sbdh.getSenderId());

        assertEquals(sbdh.getSenderId(), ParticipantIdentifier.of("0192:991974466"));
        assertEquals(sbdh.getRecipientId(), ParticipantIdentifier.of("0192:889640782"));

        assertEquals(
                sbdh.getDocumentTypeIdentifier(),
                PeppolDocumentTypeIdAcronym.EHF_INVOICE.toVefa()
        );

    }

}
