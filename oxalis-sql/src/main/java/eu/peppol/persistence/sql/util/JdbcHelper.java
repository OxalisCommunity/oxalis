package eu.peppol.persistence.sql.util;

import java.util.Calendar;
import java.util.Date;

/**
 * @author steinar
 *         Date: 15.08.13
 *         Time: 16:13
 */
public class JdbcHelper {

    public static Date setEndDateIfNull(java.util.Date end) {
        if (end == null) {
            end = new java.util.Date();
        }
        return end;
    }

    public static Date setStartDateIfNull(java.util.Date start) {
        Date result = start;
        if (start == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2013, Calendar.FEBRUARY, 1);
            result = calendar.getTime();
        }

        return result;
    }
}
