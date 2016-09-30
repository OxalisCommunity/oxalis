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

package eu.peppol.as2;

import eu.peppol.security.CommonName;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 08.12.13
 *         Time: 20:58
 */
public class PeppolAs2SystemIdentifierTest {
    @Test
    public void testValueOf() throws Exception {
        CommonName commonName = new CommonName("AP_10000006");

        PeppolAs2SystemIdentifier p1 = PeppolAs2SystemIdentifier.valueOf(commonName);
        PeppolAs2SystemIdentifier p2 = PeppolAs2SystemIdentifier.valueOf(new CommonName("AP_10000006"));

        assertEquals(p1, p2);
    }
}
