package eu.peppol.start.identifier;

import org.testng.annotations.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * User: andy
 * Date: 4/11/12
 * Time: 11:19 AM
 */
public class ParticipantIdTest {



    @Test
    public void testIsValid() {

        // a valid orgNo
        assertTrue(ParticipantId.isValidOrganisationNumber("968218743"));

        // not valid
        assertFalse(ParticipantId.isValidOrganisationNumber("123456789"));

        // null
        assertFalse(ParticipantId.isValidOrganisationNumber((String) null));

        // empty String
        assertFalse(ParticipantId.isValidOrganisationNumber(""));

        // modulus on sums = 0
        assertTrue(ParticipantId.isValidOrganisationNumber("961329310"));

    }
}
