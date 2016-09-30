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

package eu.peppol.statistics;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 26.03.13
 *         Time: 09:22
 */
public class StatisticsGranularityTest {
    @Test
    public void testValueForAbbreviation() throws Exception {
        StatisticsGranularity g = StatisticsGranularity.valueForAbbreviation("m");
        assertEquals(g, StatisticsGranularity.MONTH);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullAbbreviation()  {
        StatisticsGranularity g = StatisticsGranularity.valueForAbbreviation(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidAbbreviation()  {
        StatisticsGranularity g = StatisticsGranularity.valueForAbbreviation("x");
    }
}
