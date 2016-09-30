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

package eu.peppol.transport;

import eu.peppol.MessageDigestResult;
import org.testng.annotations.Test;

import java.security.MessageDigest;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 17.11.2015
 *         Time: 19.15
 */
public class MessageDigestResultTest {

    @Test
    public void testGetDigestAsString() throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update("The quick brown fox jumped over the lazy dog".getBytes());
        byte[] digest = md.digest();

        MessageDigestResult messageDigestResult = new MessageDigestResult(digest, "SHA-256");
        String digestAsString = messageDigestResult.getDigestAsString();
        assertNotNull(digestAsString);
    }
}