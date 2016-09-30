/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.util;

import org.testng.annotations.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author steinar
 *         Date: 09.03.2016
 *         Time: 11.18
 */
public class CaseInsensitiveComparatorTest {

    @Test
    public void testCompare() throws Exception {

        Map<String, String> m = new TreeMap<String, String>(new CaseInsensitiveComparator());
        m.put("nAmE", "value");
        m.put("Header-Name", "header-value");
        m.put("null", null);
        m.put(null, null);

        assertEquals(m.get("nAmE"), "value");
        assertEquals(m.get("header-name"),"header-value");
        assertNull(m.get("null"));
        assertNull(m.get(null), null);

        m.put(null, "X");
        assertEquals(m.get(null), "X");

    }
}