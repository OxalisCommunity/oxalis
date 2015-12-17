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

/* Created by steinar on 20.05.12 at 12:14 */
package eu.peppol.start.identifier;

import eu.peppol.identifier.CustomizationIdentifier;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 *
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class CustomizationIdentifierTest {

    @Test
    public void parseEhfKreditNota() {
        CustomizationIdentifier customizationIdentifier = CustomizationIdentifier.valueOf("urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.cenbii.eu:profile:biixx:ver1.0#urn:www.difi.no:ehf:kreditnota:ver1");
    }

    @Test
    public void parseApplicationResponse() {
        final String s = "urn:www.cenbii.eu:transaction:biicoretrdm057:ver1.0:#urn:www.peppol.eu:bis:peppol1a:ver1.0";
        CustomizationIdentifier customizationIdentifier = CustomizationIdentifier.valueOf(s);
        assertEquals(customizationIdentifier.toString(), s);
    }

    @Test
    public void equalsTest() {
        final String s = "urn:www.cenbii.eu:transaction:biicoretrdm057:ver1.0:#urn:www.peppol.eu:bis:peppol1a:ver1.0";
        CustomizationIdentifier c1 = CustomizationIdentifier.valueOf(s);
        CustomizationIdentifier c2 = CustomizationIdentifier.valueOf(s);

        assertEquals(c1,c2);
    }

    @Test
    public void valueOfEqualsAndEquals() throws Exception {
        String value = "aamund var her";
        CustomizationIdentifier customizationIdentifier = CustomizationIdentifier.valueOf(value);
        assertEquals(customizationIdentifier.toString(), value);

    }
}
