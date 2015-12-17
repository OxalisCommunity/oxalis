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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 09.10.13
 *         Time: 10:41
 */
@Test(groups = {"soc"})
public class As2SystemIdentifierTest {

    @Test(groups = {"soc"})
    public void testToString() throws Exception {

        As2SystemIdentifier ap_0001 = new As2SystemIdentifier("AP_0001");
        assertEquals(ap_0001.toString(), "AP_0001");
    }

    @Test(expectedExceptions = InvalidAs2SystemIdentifierException.class)
    public void testInvalidAs2Name() throws Exception {

        As2SystemIdentifier as2SystemIdentifier = new As2SystemIdentifier("\\ap32");
    }

    public void testEquals() throws Exception {

        As2SystemIdentifier first = new As2SystemIdentifier("AP_00032");
        As2SystemIdentifier second = new As2SystemIdentifier("AP_00032");
        assertEquals(first,second);

        assertEquals(first.hashCode(), second.hashCode());
    }
}
