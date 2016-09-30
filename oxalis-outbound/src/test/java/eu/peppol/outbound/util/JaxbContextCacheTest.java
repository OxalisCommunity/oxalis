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

package eu.peppol.outbound.util;

import eu.peppol.util.JaxbContextCache;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBContext;

import static org.testng.Assert.assertTrue;

/**
 * User: nigel
 * Date: Oct 25, 2011
 * Time: 9:05:52 AM
 */
@Test
public class JaxbContextCacheTest {

    public void test01() throws Throwable {

        JAXBContext context1 = JaxbContextCache.getInstance(String.class);
        JAXBContext context2 = JaxbContextCache.getInstance(String.class);
        assertTrue(context1 == context2);

    }
}
