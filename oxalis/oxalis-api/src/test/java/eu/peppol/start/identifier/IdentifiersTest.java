package eu.peppol.start.identifier;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * User: nigel
 * Date: Dec 6, 2011
 * Time: 4:07:15 PM
 */
@Test
public class IdentifiersTest {
    public void test01() throws Throwable {

        validParticipantId("9908:976098897");

        invalidParticipantId(null);
        invalidParticipantId("");
        invalidParticipantId("9908:976098897 ");
        invalidParticipantId("990:976098897");
        invalidParticipantId("990976098897");
        invalidParticipantId("9909:976098896");
        invalidParticipantId("9908:976098896");
    }

    public void test02() throws Throwable {

        validOrganisationNo("123456785");
        validOrganisationNo("976098897");
        validOrganisationNo("991618112");

        invalidOrganisationNo(null);
        invalidOrganisationNo("");
        invalidOrganisationNo("976098897 ");
        invalidOrganisationNo("12345678-");
        invalidOrganisationNo("-23456789");
        invalidOrganisationNo("123456784");
        invalidOrganisationNo("323456780");
    }

    private void invalidOrganisationNo(String org) {
        assertEquals(ParticipantId.isValidOrganisationNumber(org), false);
    }

    private void validOrganisationNo(String org) {
        assertEquals(ParticipantId.isValidOrganisationNumber(org), true);
    }

    private void invalidParticipantId(String value) {
        assertEquals(ParticipantId.isValidParticipantIdentifier(value), false);
    }

    private void validParticipantId(String value) {
        assertEquals(ParticipantId.isValidParticipantIdentifier(value), true);
    }
}
