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

package eu.peppol.smp;

import eu.peppol.util.ConnectionException;
import org.testng.annotations.Test;

import java.net.URL;

/**
 * @author steinar
 *         Date: 18.12.13
 *         Time: 22:45
 */
public class SmpContentRetrieverImplTest {

    @Test(expectedExceptions = ConnectionException.class)
    public void test404() throws Exception {
        new SmpContentRetrieverImpl().getUrlContent(new URL("http://smp.difi.no/iso6523-actorid-upis%3A%3A9908%3A9854323/"));
    }
}
