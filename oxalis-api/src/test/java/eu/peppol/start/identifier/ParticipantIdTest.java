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

package eu.peppol.start.identifier;

import eu.peppol.identifier.ParticipantId;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * User: andy
 * Date: 4/11/12
 * Time: 11:19 AM
 */
public class ParticipantIdTest {

    @Test
    public void isValidOrganisationNumber() {

        // a valid orgNo
        assertTrue(ParticipantId.isValidNorwegianOrganisationNumber("968218743"));
    }

    @Test
    public void isNotvalidOrganisationNumber() {

        // not valid
        assertFalse(ParticipantId.isValidNorwegianOrganisationNumber("123456789"));
    }

    @Test
    public void nullIsInvalid() throws Exception {
        // null
        assertFalse(ParticipantId.isValidNorwegianOrganisationNumber((String) null));
    }

    @Test
    public void emptyStringIsInvalid() throws Exception {
        // empty String
        assertFalse(ParticipantId.isValidNorwegianOrganisationNumber(""));
    }

    @Test
    public void modulus0IsValid() {
        // modulus on sums = 0
        assertTrue(ParticipantId.isValidNorwegianOrganisationNumber("961329310"));
    }

    @Test
    public void lengthExceeds10isInvalid() {
        assertFalse(ParticipantId.isValidNorwegianOrganisationNumber("9020177699"));
        assertFalse(ParticipantId.isValidParticipantIdentifier("9908:9020177699"));
    }

}
