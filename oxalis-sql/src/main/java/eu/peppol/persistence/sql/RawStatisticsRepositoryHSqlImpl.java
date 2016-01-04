package eu.peppol.persistence.sql;

import eu.peppol.persistence.sql.util.DataSourceHelper;
import eu.peppol.persistence.sql.util.JdbcHelper;
import eu.peppol.statistics.StatisticsGranularity;
import eu.peppol.statistics.StatisticsTransformer;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;

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
	public String getPersistSqlQueryText() {
		return String.format("INSERT INTO %s (ap, tstamp,  direction, sender, receiver, doc_type, profile, channel) values(?,?,?,?,?,?,?,?)", RAW_STATS_TABLE_NAME);
	}

	/**
	 * Composes the SQL query for retrieval of statistical data between a start and end data, with
	 * a granularity as supplied.
	 *
	 * @param granularity the granularity of the statics period reported.
	 */
    @Override
	public String getRawStatisticsSqlQueryText(StatisticsGranularity granularity) {
		String hSqlDateFormat = hSqlDateFormat(granularity);
        return "SELECT\n" +
                "  ap,\n" +
                "  'OUT' AS direction,\n" +
                "  TO_CHAR(tstamp,'" + hSqlDateFormat +"') AS period,\n" +
                "  sender AS ppid,\n" +
                "  doc_type,\n" +
                "  profile,\n" +
                "  channel,\n" +
                "  COUNT(id) AS c\n" +
                "FROM\n" +
                "  raw_stats\n" +
                "WHERE\n" +
                "  direction = 'OUT'\n" +
                "  and tstamp between ? and ?\n" +
                "GROUP BY ap,direction,period,ppid,doc_type,profile,channel\n" +
                "union\n" +
                "SELECT\n" +
                "  ap,\n" +
                "  'IN' AS direction,\n" +
                "  TO_CHAR(tstamp,'" + hSqlDateFormat +"') AS period,\n" +
                "  receiver AS ppid,\n" +
                "  doc_type,\n" +
                "  profile,\n" +
                "  channel,\n" +
                "  COUNT(id) AS c\n" +
                "FROM\n" +
                "  raw_stats\n" +
                "WHERE\n" +
                "  direction = 'IN'\n" +
                "  and tstamp between ? and ?\n" +
                "\n" +
                "GROUP BY ap,direction,period,ppid,doc_type,profile,channel\n" +
                "order by period, ap\n" +
                ";";
	}

    /**
     * Retrieves statistics and transforms it using the supplied transformer.
     */
    @Override
    public void fetchAndTransformRawStatistics(StatisticsTransformer transformer, Date start, Date end, StatisticsGranularity granularity) {

        String sql = this.getRawStatisticsSqlQueryText(granularity);

        start = JdbcHelper.setStartDateIfNull(start);
        end = JdbcHelper.setEndDateIfNull(end);

        Connection con = null;
        PreparedStatement ps;
        try {
            con = dataSourceHelper.getConnectionWithAutoCommit();
            ps = con.prepareStatement(sql);

            // Sets the start and end parameters for both parts of the SELECT UNION
            ps.setTimestamp(1, new Timestamp(start.getTime()));
            ps.setTimestamp(2, new Timestamp(end.getTime()));
            ps.setTimestamp(3, new Timestamp(start.getTime()));
            ps.setTimestamp(4, new Timestamp(end.getTime()));
            ResultSet rs = ps.executeQuery();

            transformer.startStatistics(start,end);
            while (rs.next()) {
                transformer.startEntry();
                transformer.writeAccessPointIdentifier(rs.getString("ap"));
                transformer.writeDirection(rs.getString("direction"));
                transformer.writePeriod(rs.getString("period"));
                transformer.writeParticipantIdentifier(rs.getString("ppid"));
                transformer.writeDocumentType(rs.getString("doc_type"));
                transformer.writeProfileId(rs.getString("profile"));
                transformer.writeChannel(rs.getString("channel"));
                transformer.writeCount(rs.getInt("c"));
                transformer.endEntry();
            }
            transformer.endStatistics();
        } catch (SQLException e) {
            throw new IllegalStateException("SQL error:" + e, e);
        } finally {
            DataSourceHelper.close(con);
        }
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
