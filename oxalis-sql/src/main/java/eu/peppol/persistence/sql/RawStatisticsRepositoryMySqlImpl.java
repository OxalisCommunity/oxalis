package eu.peppol.persistence.sql;

import eu.peppol.statistics.StatisticsGranularity;
import javax.sql.DataSource;

/**
 * This is RawStatisticsRepository implementation for running the statistics database on MySql backend, through JDBC.
 *
 * @author steinar
 * @author zeko78
 */
public class RawStatisticsRepositoryMySqlImpl extends RawStatisticsRepositoryJdbcImpl {

    public RawStatisticsRepositoryMySqlImpl(DataSource dataSource) {
		super(dataSource);
    }

	/**
 	 * Composes the SQL query to persist raw statistics into the DBMS.
	 */
    @Override
	String getPersistSqlQueryText() {
		return String.format("INSERT INTO %s (ap, tstamp,  direction, sender, receiver, doc_type, profile, channel, messageUID) values(?,?,?,?,?,?,?,?,?)", RAW_STATS_TABLE_NAME);
	}

	/**
	 * Composes the SQL query for retrieval of statistical data between a start and end data, with
	 * a granularity as supplied.
	 *
	 * @param granularity the granularity of the statics period reported.
	 */
    @Override
	String getRawStatisticsSqlQueryText(StatisticsGranularity granularity) {
		String mySqlDateFormat = mySqlDateFormat(granularity);
        return "SELECT\n" +
                "  ap,\n" +
                "  'OUT' direction,\n" +
                "  date_format(tstamp,'" + mySqlDateFormat +"') period,\n" +
                "  sender ppid,\n" +
                "  doc_type,\n" +
                "  profile,\n" +
                "  channel,\n" +
                "  messageUID,\n" +
                "  COUNT(*) count\n" +
                "FROM\n" +
                "  oxa_raw_stats\n" +
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
                "  messageUID,\n" +
                "  COUNT(*) count\n" +
                "FROM\n" +
                "  oxa_raw_stats\n" +
                "WHERE\n" +
                "  direction = 'IN'\n" +
                "  and tstamp between ? and ?\n" +
                "\n" +
                "GROUP BY 1,2,3,4,5,6,7\n" +
                "order by period, ap\n" +
                ";";
	}

	/**
	 * Return the correct date_format parameter for the chosen granularity
	 */
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
