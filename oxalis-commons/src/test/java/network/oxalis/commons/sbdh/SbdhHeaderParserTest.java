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

package network.oxalis.commons.sbdh;

import network.oxalis.api.header.HeaderParser;
import network.oxalis.api.lang.OxalisContentException;
import network.oxalis.commons.header.SbdhHeaderParser;
import network.oxalis.vefa.peppol.common.model.Header;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.assertNull;

/**
 * @author steinar
 * Date: 24.06.15
 * Time: 15.58
 */
public class SbdhHeaderParserTest {

    public static final String EHF_INVOICE_NO_SBDH_XML = "/ehf-invoice-no-sbdh.xml";

    private static final HeaderParser PARSER = new SbdhHeaderParser();

    @Test
    public void simpleConstructor() {
        new SbdhHeaderParser();
    }

    @Test(expectedExceptions = OxalisContentException.class)
    public void parseXmlFileWithoutSBDH() throws OxalisContentException {
        InputStream resourceAsStream = getClass().getResourceAsStream(EHF_INVOICE_NO_SBDH_XML);

        Header sbdh = PARSER.parse(resourceAsStream);
        assertNull(sbdh);
    }
}
