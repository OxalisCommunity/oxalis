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

package eu.peppol.identifier;

import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

/**
 * @author steinar
 * @author thore
 */
public class MessageIdTest {

    @Test
    public void testUuidWithPrefix() throws Exception {
        String uuidWithPrefix = "uuid:" + UUID.randomUUID();
        MessageId messageId = new MessageId(uuidWithPrefix);
        UUID uuid = messageId.toUUID();
        assertFalse(uuid.toString().startsWith("uuid")); // double check that "uuid:" is not here
        assertEquals(messageId.stringValue(), uuidWithPrefix);
        assertEquals(messageId.toString(), uuidWithPrefix);
    }

    @Test
    public void testUuidWithoutPrefix() throws Exception {
        String uuidWithoutPrefix = "" + UUID.randomUUID();
        MessageId messageId = new MessageId(uuidWithoutPrefix);
        UUID uuid = messageId.toUUID();
        assertFalse(uuid.toString().startsWith("uuid")); // double check that "uuid:" is not here
        assertEquals(messageId.stringValue(), uuidWithoutPrefix);
        assertEquals(messageId.toString(), uuidWithoutPrefix);
    }

    @Test
    public void knownUuidWithoutPrefix() throws Exception {
        String knownUuid = "1070e7f0-3bae-11e3-aa6e-0800200c9a66";
        MessageId messageId = new MessageId(knownUuid);
        UUID uuid = messageId.toUUID();
        assertEquals(uuid.toString(), knownUuid);
        assertEquals(messageId.toString(), knownUuid); // stringValue and toString should be the same
    }

    @Test
    public void knownUuidWithPrefix() throws Exception {
        String knownUuidWithPrefix = "uuid:1070e7f0-3bae-11e3-aa6e-0800200c9a66";
        MessageId messageId = new MessageId(knownUuidWithPrefix);
        UUID uuid = messageId.toUUID();
        assertFalse(uuid.toString().startsWith("uuid")); // double check that "uuid:" is not here
        assertEquals(uuid.toString(), messageId.toString().substring(5)); // skip the 5 first characters ("uuid:")
        assertEquals(messageId.toString(), knownUuidWithPrefix); // stringValue and toString should be the same
    }

    @Test
    public void illegalUuidFromString() throws Exception {
        MessageId messageId = new MessageId("this-is-illegal-uuid");
        try {
            UUID uuid = messageId.toUUID();
            fail("The UUID of '" + uuid.toString() + "' should not be allowed thru");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().startsWith("Internal error in regexp. Unable to determine UUID of 'this-is-illegal-uuid'"));
        }
    }

}
