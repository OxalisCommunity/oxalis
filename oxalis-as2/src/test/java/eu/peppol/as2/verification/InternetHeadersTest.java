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

package eu.peppol.as2.verification;

import org.testng.annotations.Test;

import javax.mail.internet.InternetHeaders;
import java.util.TreeMap;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 08.03.2016
 *         Time: 17.05
 */
public class InternetHeadersTest {


    @Test
    public void caseSensitivity() throws Exception {

        InternetHeaders internetHeaders = new InternetHeaders();
        internetHeaders.addHeader("NaMe", "value");
        String[] value = internetHeaders.getHeader("name");
        assertEquals(value[0], "value");
    }

    @Test
    public void caseInsensitiveMap() throws Exception {

        TreeMap<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.put("NaMe", "value");
        String value = map.get("name");
        assertEquals(value, "value");
    }
}
