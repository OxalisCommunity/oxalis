package eu.peppol.persistence.sql;

import eu.peppol.persistence.sql.util.DataSourceHelper;
import eu.peppol.persistence.sql.util.JdbcHelper;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.StatisticsGranularity;
import eu.peppol.statistics.StatisticsTransformer;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;

/**
 * This is RawStatisticsRepository implementation for running the statistics database on MySql backend, through Jdbc.
 *
 * User: zeko78
 * Date: 07.11.14
 * Time: 10:01
 */
public class RawStatisticsRepositoryMySqlImpl extends RawStatisticsRepositoryJdbcImpl {

	/**
 	 * Composes the SQL query to persist raw statistics into the DBMS.
	 *
	 * @return
	 */
	String GetPersistSqlQueryText() {
		return String.format("INSERT INTO %s (ap, tstamp,  direction, sender, receiver, doc_type, profile, channel) values(?,?,?,?,?,?,?,?)", RAW_STATS_TABLE_NAME);
	}

	/**
	 * Composes the SQL query for retrieval of statistical data between a start and end data, with
	 * a granularity as supplied.
	 *
	 * @param granularity the granularity of the statics period reported.
	 * @return
	 */
	String GetRawStatisticsSqlQueryText(StatisticsGranularity granularity) {
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

	/**
	 * Return the currect date_format parameter for the chosen granularity
	 * @return
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
