/*
 * Copyright (c) 2015 Steinar Overbeck Cook
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