package eu.peppol.persistence.sql;

import eu.peppol.persistence.sql.dao.DimensionJdbcHelper;
import eu.peppol.persistence.sql.util.JdbcScriptRunner;
import eu.peppol.statistics.*;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;

/**
 * JDBC implementation of StatisticsRepository component supplied with Oxalis. In theory, you may use any implementation of
 * StatisticsRepository you like, however; in real life, most people will probably stick with the SQL database.
 * <p/>
 * Henceforth this implementation is located here in the commons component of Oxalis, in order to be used by either
 * the DBCP or the JNDI implementation of StatisticsRepository.
 * <p/>
 * User: steinar
 * Date: 30.01.13
 * Time: 19:32
 */
public class StatisticsRepositoryJdbcImpl implements StatisticsRepository {

    public static final String RAW_STATS_TABLE_NAME = "raw_stats";
    public static final String SQL_CREATE_STATISTICS_STAR_SCHEMA_SQL = "sql/create-statistics-star-schema.sql";
    private final DataSource dataSource;
    private final DimensionJdbcHelper dimensionJdbcHelper;

    public StatisticsRepositoryJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        dimensionJdbcHelper = new DimensionJdbcHelper();
    }


    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void createDatabaseSchemaForDataWarehouse() {
        JdbcScriptRunner jdbcScriptRunner= null;

        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            jdbcScriptRunner = new JdbcScriptRunner(connection, false, false);
            jdbcScriptRunner.setDelimiter(";", false);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to get a database connection " + e, e);
        }

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(SQL_CREATE_STATISTICS_STAR_SCHEMA_SQL);
        if (inputStream == null) {
            throw new IllegalStateException("Unable to locate resource " + SQL_CREATE_STATISTICS_STAR_SCHEMA_SQL + " in class path");
        }
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to create reader for input stream for " + SQL_CREATE_STATISTICS_STAR_SCHEMA_SQL);
        }

        try {
            jdbcScriptRunner.runScript(bufferedReader);
        } catch (IOException e) {
            throw new IllegalStateException("I/O error executing " + SQL_CREATE_STATISTICS_STAR_SCHEMA_SQL + "; " + e, e);
        } catch (SQLException e) {
            throw new IllegalStateException("SQL error executing " + SQL_CREATE_STATISTICS_STAR_SCHEMA_SQL + "; " + e, e);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to close reader");
            }
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public Integer persist(RawStatistics rawStatistics) {
        Connection con = null;
        PreparedStatement ps;

        Integer result = 0;

        try {

            con = getConnectionWithAutoCommit();

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
    public Integer persist(AggregatedStatistics statisticsEntry) {
        Integer result = null;

        Connection con = null;

        con = getConnectionNoAutoCommit();

        MessageFact messageFact = new MessageFact();

        // Retrieves the date dimension FK
        messageFact.timeId = dimensionJdbcHelper.getKeyFor(con, statisticsEntry.getDate());

        // Retrieves the access point identifier FK
        messageFact.apId = dimensionJdbcHelper.getKeyFor(con,statisticsEntry.getAccessPointIdentifier());

        // Retrieves the PEPPOL Participant identifier (PPID) FK
        messageFact.ppidId = dimensionJdbcHelper.getKeyFor(con, statisticsEntry.getParticipantId());

        // Retrieves the Document type identifier FK
        messageFact.documentTypeIdentifierId = dimensionJdbcHelper.getKeyFor(con, statisticsEntry.getPeppolDocumentTypeId());

        // Retrieves the Channel identifier FK
        messageFact.channelId = dimensionJdbcHelper.getKeyFor(con, statisticsEntry.getChannelId());

        // Retrieves the Profile dimension FK
        messageFact.profileId = dimensionJdbcHelper.getKeyFor(con, statisticsEntry.getPeppolProcessTypeId());

        messageFact.count = statisticsEntry.getCount();
        messageFact.direction = statisticsEntry.getDirection().name();

        // Persists the message fact entry

        String sql = "insert into message_fact(time_id, ap_id, ppid_id, document_id, profile_id, channel_id, direction, counter) values(?,?,?,?,?,?,?,?)";

        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, messageFact.timeId);
            ps.setInt(2, messageFact.apId);
            ps.setInt(3, messageFact.ppidId);
            ps.setInt(4, messageFact.documentTypeIdentifierId);
            ps.setInt(5, messageFact.profileId);
            ps.setInt(6, messageFact.channelId);
            ps.setString(7, messageFact.direction);
            ps.setInt(8, messageFact.count);
            int rc = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                result = new Integer(rs.getInt(1));
            }
            con.commit();

            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to execute " + sql+ "; " +e,e );
        } finally {
            close(con);
        }
    }

    private Connection getConnectionWithAutoCommit() {
        return getConnection(true);
    }

    private Connection getConnectionNoAutoCommit() {
        Connection con = getConnection(false);
        return con;
    }

    private Connection getConnection(boolean autoCommit) {
        Connection con;
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to retrieve connection " + e, e);
        }

        return con;
    }


    @Override
    public void fetchAndTransform(StatisticsTransformer transformer, Date start, Date end, StatisticsGranularity granularity) {

        String sql = SQLComposer.createSqlQueryText(granularity);

        start = setStartDateIfNull(start);
        end = setEndDateIfNull(end);

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataSource.getConnection();
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
            close(con);
        }


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
