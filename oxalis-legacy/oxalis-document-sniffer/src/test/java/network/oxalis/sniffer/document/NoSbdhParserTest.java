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
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author soc
 */
public class NoSbdhParserTest {
    
    @Test
    public void verifyCorrectParsingOfReceiver() throws Exception {

        InputStream resourceAsStream = getClass().getResourceAsStream("/issue250/Issue250-sample-invoice.xml");
        assertNotNull(resourceAsStream, "Test resource not found");

        NoSbdhParser p = new NoSbdhParser();
        PeppolStandardBusinessHeader peppolStandardBusinessHeader = p.originalParse(resourceAsStream);
        String receiver = peppolStandardBusinessHeader.getRecipientId().getIdentifier();

        assertEquals("9954:111111111", receiver);
    }
}
