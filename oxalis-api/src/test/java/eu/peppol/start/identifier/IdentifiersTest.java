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

import static org.testng.Assert.assertEquals;

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
        assertEquals(ParticipantId.isValidNorwegianOrganisationNumber(org), false);
    }

    private void validOrganisationNo(String org) {
        assertEquals(ParticipantId.isValidNorwegianOrganisationNumber(org), true);
    }

    private void invalidParticipantId(String value) {
        assertEquals(ParticipantId.isValidParticipantIdentifier(value), false);
    }

    private void validParticipantId(String value) {
        assertEquals(ParticipantId.isValidParticipantIdentifier(value), true);
    }
}
