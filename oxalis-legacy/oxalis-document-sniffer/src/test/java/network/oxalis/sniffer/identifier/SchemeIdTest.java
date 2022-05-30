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

package network.oxalis.sniffer.identifier;

import network.oxalis.vefa.peppol.icd.api.Icd;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 * Date: 10.11.2016
 * Time: 11.54
 */
public class SchemeIdTest {

    @Test
    public void testBelgianCrossroadBankOfEnterprises() throws Exception {
        Icd sid = SchemeId.parse("BE:CBE");
        assertEquals(sid.getIdentifier(), "BE:CBE");
        assertEquals(sid.getCode(), "9956");
        assertEquals(SchemeId.fromISO6523("9956"), sid);
    }
}
