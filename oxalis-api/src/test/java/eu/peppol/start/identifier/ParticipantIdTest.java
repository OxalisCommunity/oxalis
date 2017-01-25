/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.peppol.start.identifier;

import eu.peppol.identifier.InvalidPeppolParticipantException;
import eu.peppol.identifier.ParticipantId;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
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
    public void modulus0IsValid() {
        // modulus on sums = 0
        assertTrue(ParticipantId.isValidNorwegianOrganisationNumber("961329310"));
    }

    @Test
    public void testWithSpaces() {
        ParticipantId participantId = ParticipantId.valueOf(" NO 976098897 MVA  ");
        assertNotNull(participantId);
    }

    @Test
    public void testSample() {
        ParticipantId participantId = ParticipantId.valueOf("9908:810018909");

    }

    @Test(expectedExceptions = {InvalidPeppolParticipantException.class})
    public void testInvalidScheme() {
        ParticipantId no976098897 = ParticipantId.valueOf("0001:976098897");
    }

    @Test(expectedExceptions = InvalidPeppolParticipantException.class)
    public void testOrgIdWithNoDigits() {
        ParticipantId.valueOf("sender");
    }
}
