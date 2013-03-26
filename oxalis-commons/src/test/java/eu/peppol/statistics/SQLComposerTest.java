package eu.peppol.statistics;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 26.03.13
 *         Time: 10:38
 */
public class SQLComposerTest {
    @Test
    public void testCreateSqlQueryText() throws Exception {
        String s = SQLComposer.createSqlQueryText(StatisticsGranularity.HOUR);
    }

    @Test
    public void testMySqlDateFormatYear() throws Exception {
        String s = SQLComposer.mySqlDateFormat(StatisticsGranularity.YEAR);
        assertEquals(s, "%Y");
    }

    @Test
    public void testMySqlDateFormatMonth() throws Exception {
        String s = SQLComposer.mySqlDateFormat(StatisticsGranularity.MONTH);
        assertEquals(s, "%Y-%m");
    }

    @Test
    public void testMySqlDateFormatDay() throws Exception {
        String s = SQLComposer.mySqlDateFormat(StatisticsGranularity.DAY);
        assertEquals(s, "%Y-%m-%d");
    }

    @Test
    public void testMySqlDateFormatHour() throws Exception {
        String s = SQLComposer.mySqlDateFormat(StatisticsGranularity.HOUR);
        assertEquals(s, "%Y-%m-%dT%h");
    }

}
