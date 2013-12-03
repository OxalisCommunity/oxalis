package eu.peppol.persistence.sql;

import eu.peppol.persistence.sql.dao.DimensionJdbcHelper;
import eu.peppol.persistence.sql.util.DataSourceHelper;
import eu.peppol.persistence.sql.util.JdbcHelper;
import eu.peppol.persistence.sql.util.JdbcScriptRunner;
import eu.peppol.statistics.AggregatedStatistics;
import eu.peppol.statistics.AggregatedStatisticsRepository;
import eu.peppol.statistics.ResultSetWriter;
import eu.peppol.statistics.StatisticsGranularity;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.util.Date;

/**
 * Jdbc implementation of AggregatedStatisticsRepository.
 *
 * @author steinar
 *         Date: 15.08.13
 *         Time: 16:09
 */
public class
        AggregatedStatisticsRepositoryJdbcImpl implements AggregatedStatisticsRepository {

    static final String SQL_CREATE_STATISTICS_STAR_SCHEMA_SQL = "sql/create-statistics-star-schema.sql";

    private final DataSourceHelper dataSourceHelper;
    private final DimensionJdbcHelper dimensionJdbcHelper;

    public AggregatedStatisticsRepositoryJdbcImpl(DataSource dataSource) {
        dataSourceHelper = new DataSourceHelper(dataSource);
        dimensionJdbcHelper = DimensionJdbcHelper.SINGLETON;
    }

    @Override
    public void createDatabaseSchemaForDataWarehouse() {
        JdbcScriptRunner jdbcScriptRunner= null;

        Connection connection = null;

        connection = dataSourceHelper.getConnectionWithAutoCommit();
        jdbcScriptRunner = new JdbcScriptRunner(connection, false, false);
        jdbcScriptRunner.setDelimiter(";", false);

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
    public Integer persist(AggregatedStatistics statisticsEntry) {
        Integer result = null;

        Connection con = null;

        con = dataSourceHelper.getConnectionNoAutoCommit();

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
            if (messageFact.channelId != null) {
                ps.setInt(6, messageFact.channelId );
            } else {
                ps.setNull(6, Types.INTEGER);
            }
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
            DataSourceHelper.close(con);
        }
    }



    @Override
    public void selectAggregatedStatistics(ResultSetWriter resultSetWriter, Date start, Date end, StatisticsGranularity granularity) {

        String sql = SQLComposer.createAggregatedStatisticsSqlQueryText();

        start = JdbcHelper.setStartDateIfNull(start);
        end = JdbcHelper.setEndDateIfNull(end);

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataSourceHelper.getConnectionNoAutoCommit();
            ps = con.prepareStatement(sql);

            // Sets the start and end parameters for both parts of the SELECT UNION
            ps.setTimestamp(1, new java.sql.Timestamp(start.getTime()));
            ps.setTimestamp(2, new Timestamp(end.getTime()));
            ResultSet rs = ps.executeQuery();

            // Dumps the lot
            resultSetWriter.writeAll(rs);

        } catch (SQLException e) {
            throw new IllegalStateException("SQL error:" + e, e);
        } finally {
            DataSourceHelper.close(con);
        }
    }

    @Override
    public void close() {
        dimensionJdbcHelper.closeCache();
    }
}
