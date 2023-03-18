/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package network.oxalis.sniffer.identifier;

import network.oxalis.sniffer.lang.InvalidPeppolParticipantException;
import network.oxalis.vefa.peppol.icd.code.PeppolIcd;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.testng.Assert.*;

/**
 * @author andy
 * @author thore
 */
public class PeppolParticipantIdTest {

    // ZZZ is not allowed in PEPPOL Network.
    @Test(enabled = false)
    public void testFoedselsnummerWhichCouldBeUsedByDigitalMultiKanal() {

        // multikanal uses fødselsnummer
        ParticipantId p1 = ParticipantId.valueOf("9999:42342342343");
        assertNotNull(p1);

        // multikanal uses orgnumbers
        assertNotNull(ParticipantId.valueOf("9999:968218743"));

    }

    @Test
    public void testParsePeppolParticpantId() throws Exception {

        ParticipantId no976098897 = ParticipantId.valueOf("0192:976098897");
        assertEquals(no976098897, new ParticipantId(PeppolIcd.NO_ORG, "976098897"));

        no976098897 = ParticipantId.valueOf("0192:976098897");
        assertEquals(no976098897, new ParticipantId(PeppolIcd.NO_ORG, "976098897"));

        no976098897 = ParticipantId.valueOf("9901:976098897");
        assertEquals(no976098897, new ParticipantId(PeppolIcd.DK_CPR, "976098897"));

        //invalid iso code will not be parsed.
        try {
            ParticipantId.valueOf("0001:976098897");
            fail("Invalid scheme should not result in a participant instance");
        } catch (Exception e) {

        }

    }

    /**
     * Tests that when using value of we get null with invalid norwegian organisation numbers
     */
    @Test()
    public void testIsValidValueOf() {


        assertNotNull(ParticipantId.valueOf("0192:968218743"));

        assertNotNull(ParticipantId.valueOf("0192:NO976098897MVA"));

        assertNotNull(ParticipantId.valueOf("0192:NO 976098897 MVA"));

        assertNotNull(ParticipantId.valueOf("0192:976098897 MVA"));

        assertNotNull(ParticipantId.valueOf("0192:976098897MVA"));


    }

    @Test(expectedExceptions = {InvalidPeppolParticipantException.class})
    public void invalidOrganisationNumbers() {
        // Not a valid orgNo
        assertNotNull(ParticipantId.valueOf("968218743"));

        // not valid
        assertNull(ParticipantId.valueOf("123456789"));
        assertNull(ParticipantId.valueOf("986532933"));
        assertNull(ParticipantId.valueOf("986532952"));
        assertNull(ParticipantId.valueOf("986532954"));
        assertNull(ParticipantId.valueOf("986532955"));
        assertNotNull(ParticipantId.valueOf("988890081"));

        assertNotNull(ParticipantId.valueOf("968 218 743"));

        // null
        assertNull(ParticipantId.valueOf((String) null));

        // empty String
        assertNull(ParticipantId.valueOf(""));
    }

    @Test
    public void testOrganistaionId() throws Exception {
        ParticipantId.valueOf("0192:968218743");
    }

    @Test
    public void testOrgNumWithSpaces() throws Exception {
        ParticipantId.valueOf("0192:968 218 743");

        ParticipantId.valueOf("01 92:9682 18743");

        ParticipantId.valueOf("00 07:9682 18743");
    }


    @Test
    public void testSerialize() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        ObjectInputStream ois = null;
        try {
            final ParticipantId expectedParticipantId = ParticipantId.valueOf("0192:976098897");

            oos.writeObject(expectedParticipantId);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            ois = new ObjectInputStream(in);

            final ParticipantId peppolParticipantId = (ParticipantId) ois.readObject();
            assertEquals(peppolParticipantId, expectedParticipantId);
        } finally {
            oos.close();
            if (ois != null) {
                ois.close();
            }
        }


        assertTrue(out.toByteArray().length > 0);
    }


    @Test(enabled = false)
    public void testSRO3079() throws Exception {

        ParticipantId peppolParticipantId = ParticipantId.valueOf("9147:91723");
        assertNotNull(peppolParticipantId);

        peppolParticipantId = ParticipantId.valueOf("9957:61394");
        assertNotNull(peppolParticipantId);
    }
}
