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
