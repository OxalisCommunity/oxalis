/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.statistics.jdbc;

import network.oxalis.persistence.api.JdbcTxManager;
import network.oxalis.statistics.util.DataSourceHelper;
import network.oxalis.statistics.util.JdbcHelper;
import network.oxalis.statistics.api.RawStatistics;
import network.oxalis.statistics.api.RawStatisticsRepository;
import network.oxalis.statistics.api.StatisticsGranularity;
import network.oxalis.statistics.api.StatisticsTransformer;

import java.sql.*;
import java.util.Date;

/**
 * Basic JDBC implementation of StatisticsRepository component supplied with Oxalis.
 * In theory, you may use any implementation of StatisticsRepository you like,
 * however; in real life, most people will probably stick with the SQL database.
 *
 * @author steinar
 */
public abstract class RawStatisticsRepositoryJdbcImpl implements RawStatisticsRepository {

    public static final String RAW_STATS_TABLE_NAME = "raw_stats";

    protected final JdbcTxManager jdbcTxManager;

    public RawStatisticsRepositoryJdbcImpl(JdbcTxManager jdbcTxManager) {
        this.jdbcTxManager = jdbcTxManager;
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
            con = jdbcTxManager.getConnection();
            ps = con.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, rawStatistics.getAccessPointIdentifier().toString());
            ps.setTimestamp(2, new Timestamp(rawStatistics.getDate().getTime()));
            ps.setString(3, rawStatistics.getDirection().toString());
            ps.setString(4, rawStatistics.getSender().getIdentifier());
            ps.setString(5, rawStatistics.getReceiver().getIdentifier());
            ps.setString(6, rawStatistics.getDocumentTypeIdentifier().toString());
            ps.setString(7, rawStatistics.getProcessIdentifier().toString());
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
            con = jdbcTxManager.getConnection();
            ps = con.prepareStatement(sql);

            // Sets the start and end parameters for both parts of the SELECT UNION
            ps.setTimestamp(1, new Timestamp(start.getTime()));
            ps.setTimestamp(2, new Timestamp(end.getTime()));
            ps.setTimestamp(3, new Timestamp(start.getTime()));
            ps.setTimestamp(4, new Timestamp(end.getTime()));
            ResultSet rs = ps.executeQuery();

            transformer.startStatistics(start, end);
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
