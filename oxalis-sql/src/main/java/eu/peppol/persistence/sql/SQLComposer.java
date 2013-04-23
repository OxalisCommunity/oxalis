package eu.peppol.persistence.sql;

import eu.peppol.statistics.StatisticsGranularity;

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
    public static String createRawStatisticsSqlQueryText(StatisticsGranularity granularity) {
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

    public static String createAggregatedStatisticsSqlQueryText() {
        String sql = "SELECT\n" +
                "    time_dimension.datum,\n" +
                "    time_dimension.year,\n" +
                "    time_dimension.month,\n" +
                "    time_dimension.day,\n" +
                "    time_dimension.hour,\n" +
                "    ap_dimension.ap_code,\n" +
                "    ppid_dimension.ppid,\n" +
                "    document_dimension.document_type,\n" +
                "    document_dimension.localname,\n" +
                "    document_dimension.root_name_space,\n" +
                "    document_dimension.customization,\n" +
                "    document_dimension.version,\n" +
                "    profile_dimension.profile,\n" +
                "    channel_dimension.channel,\n" +
                "    direction,\n" +
                "    counter\n" +
                "FROM\n" +
                "    message_fact AS fact\n" +
                "natural JOIN\n" +
                "    time_dimension\n" +
                "natural JOIN\n" +
                "    ap_dimension\n" +
                "natural JOIN\n" +
                "    ppid_dimension\n" +
                "natural  JOIN\n" +
                "    document_dimension\n" +
                "left outer JOIN\n" +
                "    profile_dimension ON fact.profile_id = profile_dimension.profile_id\n" +
                "left outer JOIN channel_dimension ON fact.channel_id = channel_dimension.channel_id " +
                "where \n" +
                "        datum between ? and ?";

        return sql;
    }
}
