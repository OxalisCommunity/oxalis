/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 10.11.2016
 *         Time: 11.54
 */
public class SchemeIdTest {

    @Test
    public void testFuzzyMatchOnOrganisationIdPrefix() throws Exception {
        List<SchemeId> schemeIdList = SchemeId.fuzzyMatchOnOrganisationIdPrefix("NO976098897MVA");
        assertEquals(schemeIdList.size(), 1);
    }

    @Test
    public void testBelgianCrossroadBankOfEnterprises() throws Exception {
        SchemeId sid = SchemeId.parse("BE:CBE");
        assertEquals(sid.getSchemeId(),"BE:CBE");
        assertEquals(sid.getIso6523Icd(),"9956");
        assertEquals(SchemeId.fromISO6523("9956"), sid);
    }

}