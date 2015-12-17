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

package eu.peppol.inbound.as2;

import org.testng.annotations.BeforeMethod;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 07.10.13
 *         Time: 21:57
 */
public class As2MessageFactoryTest {


    private ServletInputStream mockServletInputStream;

    /**
     * Wraps an InputStream in a ServletInputStream as this is what is required
     * to be returned by HttpServletRequest#getInputStream()
     */
    static class MockServletInputStream extends ServletInputStream {

        private final InputStream inputStream;

        MockServletInputStream(InputStream inputStream) {

            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }

    /**
     * Wraps the test resource signed2.message in a ServletInputStream to provide sample data.
     */
    @BeforeMethod
    public void createInputStream() {
        InputStream resourceAsStream = As2MessageFactoryTest.class.getClassLoader().getResourceAsStream("signed2.message");
        assertNotNull(resourceAsStream);
    }

}
