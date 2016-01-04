package eu.peppol.persistence.sql;

import eu.peppol.statistics.StatisticsGranularity;

import javax.sql.DataSource;

/**
 * This is RawStatisticsRepository implementation for running the statistics database on MySql backend, through JDBC.
 *
 * @author steinar
 * @author zeko78
 */
public class RawStatisticsRepositoryHSqlImpl extends RawStatisticsRepositoryJdbcImpl {

    public RawStatisticsRepositoryHSqlImpl(DataSource dataSource) {
		super(dataSource);
    }

	/**
 	 * Composes the SQL query to persist raw statistics into the DBMS.
	 */
    @Override
	String getPersistSqlQueryText() {
		return String.format("INSERT INTO %s (ap, tstamp,  direction, sender, receiver, doc_type, profile, channel) values(?,?,?,?,?,?,?,?)", RAW_STATS_TABLE_NAME);
	}

	/**
	 * Composes the SQL query for retrieval of statistical data between a start and end data, with
	 * a granularity as supplied.
	 *
	 * @param granularity the granularity of the statics period reported.
	 */
    @Override
	String getRawStatisticsSqlQueryText(StatisticsGranularity granularity) {
		String hSqlDateFormat = hSqlDateFormat(granularity);
        return "SELECT\n" +
                "  ap,\n" +
                "  'OUT' direction,\n" +
                "  TO_CHAR(tstamp,'" + hSqlDateFormat +"') period,\n" +
                "  sender ppid,\n" +
                "  doc_type,\n" +
                "  profile,\n" +
                "  channel,\n" +
                "  COUNT(tstamp) count\n" +
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
                "  TO_CHAR(tstamp,'" + hSqlDateFormat +"') period,\n" +
                "  receiver ppid,\n" +
                "  doc_type,\n" +
                "  profile,\n" +
                "  channel,\n" +
                "  COUNT(tstamp) count\n" +
                "FROM\n" +
                "  raw_stats\n" +
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
    static String hSqlDateFormat(StatisticsGranularity granularity) {
        switch (granularity) {
            case YEAR:
                return "YY";
            case MONTH:
                return "YY-MM";
            case DAY:
                return "YY-MM-DD";
            case HOUR:
                return "YY-MM-DD\"T\"HH24";
            default:
                throw new IllegalArgumentException("Unable to convert " + granularity + " into a MySQL date_format() string");
        }
    }

}
