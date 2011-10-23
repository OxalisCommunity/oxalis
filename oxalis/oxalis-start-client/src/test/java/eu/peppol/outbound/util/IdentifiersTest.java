package eu.peppol.outbound.util;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * User: nigel
 * Date: Oct 21, 2011
 * Time: 10:07:05 AM
 */
@Test
public class IdentifiersTest extends TestBase {

    public void test01() throws Throwable {
        try {

            validParticipantId("9909:976098897");

            invalidParticipantId(null);
            invalidParticipantId("");
            invalidParticipantId("9909:976098897 ");
            invalidParticipantId("990:976098897");
            invalidParticipantId("990976098897");
            invalidParticipantId("9909:976098896");
            invalidParticipantId("9908:976098896");

        } catch (Throwable t) {
            signal(t);
        }
    }

    public void test02() throws Throwable {
        try {

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

        } catch (Throwable t) {
            signal(t);
        }
    }

    private void invalidOrganisationNo(String org) {
        assertEquals(Identifiers.isValidOrganisationNumber(org), false);
    }

    private void validOrganisationNo(String org) {
        assertEquals(Identifiers.isValidOrganisationNumber(org), true);
    }

    private void invalidParticipantId(String value) {
        assertEquals(Identifiers.isValidParticipantIdentifier(value), false);
    }

    private void validParticipantId(String value) {
        assertEquals(Identifiers.isValidParticipantIdentifier(value), true);
    }
}
