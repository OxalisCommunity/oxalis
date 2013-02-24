package eu.peppol.statistics;

import javax.print.attribute.standard.DateTimeAtCompleted;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * JDBC implementation of StatisticsRepository component supplied with Oxalis. In theory, you may use any implementation of
 * StatisticsRepository you like, however; in real life, most people will probably stick with the SQL database.
 * <p/>
 * Henceforth this implementation is located here in the commons component of Oxalis, in order to be used by either
 * the DBCP or the JNDI implementation of StatisticsRepository.
 * <p/>
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
            ps = con.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS);

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

    @Override
    public void fetchAndTransform(StatisticsTransformer transformer, java.util.Date start, java.util.Date end) {

        String sql = createSqlQueryText(start, end);

        start = setStartDateIfNull(start);
        end = setEndDateIfNull(end);

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement(sql);
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
            close(con);
        }


    }

    private String createSqlQueryText(java.util.Date start, java.util.Date end) {

        String sql = "SELECT\n" +
                "  ap,\n" +
                "  'OUT' direction,\n" +
                "  date_format(tstamp,'%Y-%m-%d') period,\n" +
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
                "GROUP BY 1,2, 3, 4, 5,6\n" +
                "union\n" +
                "SELECT\n" +
                "  ap,\n" +
                "  'IN' direction,\n" +
                "  date_format(tstamp,'%Y-%m-%d') period,\n" +
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
                "GROUP BY 1,2, 3, 4, 5,6\n" +
                "\n" +
                "order by period, ap\n" +
                "\n" +
                ";";

        return sql;

    }

    private Date setEndDateIfNull(java.util.Date end) {
        if (end == null) {
            end = new java.util.Date();
        }
        return end;
    }

    private Date setStartDateIfNull(java.util.Date start) {
        Date result = start;
        if (start == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2013, Calendar.FEBRUARY, 1);
            result = calendar.getTime();
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
