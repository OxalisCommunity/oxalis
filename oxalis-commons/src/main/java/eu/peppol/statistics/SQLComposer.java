package eu.peppol.statistics;

import java.util.Date;

/**
 * @author steinar
 *         Date: 26.03.13
 *         Time: 10:35
 */
class SQLComposer {

    /**
     * Composes the SQL query for retrieval of statistical data between a start and end data, with
     * a granularity as supplied.
     *
     * @param granularity the granularity of the statics period reported.
     * @return
     */
    public static String createSqlQueryText(StatisticsGranularity granularity) {
        String mySqlDateFormat = mySqlDateFormat(granularity);

        String sql = "SELECT\n" +
                "  ap,\n" +
                "  'OUT' direction,\n" +
                "  date_format(tstamp,'" + mySqlDateFormat +"') period,\n" +
                "  sender ppid,\n" +
                "  doc_type,\n" +
                "  profile,\n" +
                "  channel,\n" +
                "  COUNT(*) count\n" +
                "FROM\n" +
                "  raw_stats\n" +
                "WHERE\n" +
                "  direction = 'OUT'\n" +
                "  and tstamp between ? and ?\n" +
                "GROUP BY 1,2,3,4,5,6,7\n" +
                "union\n" +
                "SELECT\n" +
                "  ap,\n" +
                "  'IN' direction,\n" +
                "  date_format(tstamp,'" + mySqlDateFormat +"') period,\n" +
                "  receiver ppid,\n" +
                "  doc_type,\n" +
                "  profile,\n" +
                "  channel,\n" +
                "  COUNT(*) count\n" +
                "FROM\n" +
                "  raw_stats\n" +
                "WHERE\n" +
                "  direction = 'IN'\n" +
                "  and tstamp between ? and ?\n" +
                "\n" +
                "GROUP BY 1,2,3,4,5,6,7\n" +
                "order by period, ap\n" +
                ";";

        return sql;
    }


    static String mySqlDateFormat(StatisticsGranularity granularity) {
        switch (granularity) {
            case YEAR:
                return "%Y";
            case MONTH:
                return "%Y-%m";
            case DAY:
                return "%Y-%m-%d";
            case HOUR:
                return "%Y-%m-%dT%h";
            default:
                throw new IllegalArgumentException("Unable to convert " + granularity + " into a MySQL date_format() string");
        }
    }

}
