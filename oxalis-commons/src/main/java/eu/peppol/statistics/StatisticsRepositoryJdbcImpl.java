package eu.peppol.statistics;

import javax.sql.DataSource;
import java.sql.*;

/**
 * JDBC implementation of StatisticsRepository component supplied with Oxalis. In theory, you may use any implementation of
 * StatisticsRepository you like, however; in real life, most people will probably stick with the SQL database.
 *
 * Henceforth this implementation is located here in the commons component of Oxalis, in order to be used by either
 * the DBCP or the JNDI implementation of StatisticsRepository.
 *
 * <p/>
 * User: steinar
 * Date: 30.01.13
 * Time: 19:32
 */
public class StatisticsRepositoryJdbcImpl implements StatisticsRepository {

    public static final String RAW_STATS_TABLE_NAME = "raw_stats";
    private final DataSource dataSource;

    public StatisticsRepositoryJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public Integer persist(RawStatistics rawStatistics) {
        Connection con = null;
        PreparedStatement ps;

        Integer result = 0;

        try {

            try {
                con = dataSource.getConnection();
                con.setAutoCommit(true);
            } catch (SQLException e) {
                throw new IllegalStateException("Unable to create JDBC connection. " + e, e);
            }
            String sqlStatement = String.format("INSERT INTO %s (ap, tstamp,  direction, sender, receiver, doc_type, profile, channel) values(?,?,?,?,?,?,?,?)", RAW_STATS_TABLE_NAME);
            ps = con.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS );

            ps.setString(1, rawStatistics.getAccessPointIdentifier().toString());
            ps.setTimestamp(2, new Timestamp(rawStatistics.getDate().getTime()));
            ps.setString(3, rawStatistics.getDirection().toString());
            ps.setString(4, rawStatistics.getSender().stringValue());
            ps.setString(5, rawStatistics.getReceiver().stringValue());
            ps.setString(6, rawStatistics.getPeppolDocumentTypeId().toString());
            ps.setString(7, rawStatistics.getPeppolProcessTypeId().toString());
            ps.setString(8, rawStatistics.getChannelId() == null ? null : rawStatistics.getChannelId().stringValue());

            int rc = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                result = rs.getInt(1);
                rs.close();
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Unable to execute statement " + e, e);
        } finally {
            close(con);
        }
        return result;
    }


    void close(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                throw new IllegalStateException("Unable to close JDBC connection " + con);
            }
        }
    }
}
