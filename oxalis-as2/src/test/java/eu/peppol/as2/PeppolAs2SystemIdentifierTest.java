/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
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
