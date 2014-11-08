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
 * Basic JDBC implementation of StatisticsRepository component supplied with Oxalis.
 * In theory, you may use any implementation of StatisticsRepository you like,
 * however; in real life, most people will probably stick with the SQL database.
 * <p/>
 * Henceforth this implementation is located here in the commons component of Oxalis,
 * in order to be used by either the DBCP or the JNDI implementation of StatisticsRepository.
 * <p/>
 *
 * @author steinar
 */
public abstract class RawStatisticsRepositoryJdbcImpl implements RawStatisticsRepository {

    public static final String RAW_STATS_TABLE_NAME = "raw_stats";
    final DataSourceHelper dataSourceHelper;

	public RawStatisticsRepositoryJdbcImpl(DataSource dataSource) {
        dataSourceHelper = new DataSourceHelper(dataSource);
    }

    /**
     * Persists raw statistics into the DBMS via JDBC, no caching is utilized.
     */
    @Override
    public Integer persist(RawStatistics rawStatistics) {
        Connection con = null;
        PreparedStatement ps;
        Integer result = 0;
        try {

            String sqlStatement = this.getPersistSqlQueryText();
            con = dataSourceHelper.getConnectionWithAutoCommit();
            ps = con.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, rawStatistics.getAccessPointIdentifier().toString());
            ps.setTimestamp(2, new Timestamp(rawStatistics.getDate().getTime()));
            ps.setString(3, rawStatistics.getDirection().toString());
            ps.setString(4, rawStatistics.getSender().stringValue());
            ps.setString(5, rawStatistics.getReceiver().stringValue());
            ps.setString(6, rawStatistics.getPeppolDocumentTypeId().toString());
            ps.setString(7, rawStatistics.getPeppolProcessTypeId().toString());
            ps.setString(8, rawStatistics.getChannelId() == null ? null : rawStatistics.getChannelId().stringValue());

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                result = rs.getInt(1);
                rs.close();
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Unable to execute statement " + e, e);
        } finally {
            DataSourceHelper.close(con);
        }
        return result;
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
            ps.setTimestamp(1, new java.sql.Timestamp(start.getTime()));
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
                transformer.writeCount(rs.getInt("count"));
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
 	 * Composes the SQL query to persist raw statistics into the DBMS.
	 */
	abstract String getPersistSqlQueryText();

	/**
	 * Composes the SQL query for retrieval of statistical data between a start and end data,
	 * with a granularity as supplied.
	 *
	 * @param granularity the granularity of the statics period reported.
	 */
	abstract String getRawStatisticsSqlQueryText(StatisticsGranularity granularity);

}
