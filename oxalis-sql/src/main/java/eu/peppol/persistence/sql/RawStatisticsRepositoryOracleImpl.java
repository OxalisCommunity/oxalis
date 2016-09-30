/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.persistence.sql;

import eu.peppol.persistence.sql.util.DataSourceHelper;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.StatisticsGranularity;

import javax.sql.DataSource;
import java.sql.*;

/**
 * This is RawStatisticsRepository implementation for running the statistics database on Oracle backend, through JDBC.
 *
 * @author thore
 */
public class RawStatisticsRepositoryOracleImpl extends RawStatisticsRepositoryJdbcImpl {

    public RawStatisticsRepositoryOracleImpl(DataSource dataSource) {
		super(dataSource);
    }

    @Override
    public Integer persist(RawStatistics rawStatistics) {
        Connection con = null;
        PreparedStatement ps;
        Integer result = 0;
        try {
            con = dataSourceHelper.getConnectionWithAutoCommit();
            String sqlStatement = this.getPersistSqlQueryText();

            // Oracle does not support Statement.RETURN_GENERATED_KEYS, so return the trigger generated "id" column
            ps = con.prepareStatement(sqlStatement, new String[]{"id"});
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

    @Override
    String getPersistSqlQueryText() {
        return String.format("INSERT INTO %s (ap, tstamp,  direction, sender, receiver, doc_type, profile, channel) values (?,?,?,?,?,?,?,?)", RAW_STATS_TABLE_NAME);
    }

    @Override
	String getRawStatisticsSqlQueryText(StatisticsGranularity granularity) {
		String dateFormatWithSelectedGranularity = oracleDateFormat(granularity);
        return "SELECT\n" +
                "  ap,\n" +
                "  'OUT' direction,\n" +
                "  TO_CHAR(tstamp,'" + dateFormatWithSelectedGranularity +"') period,\n" +
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
                "  TO_CHAR(tstamp,'" + dateFormatWithSelectedGranularity +"') period,\n" +
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
                "order by period, ap\n";
	}

	/**
	 * Return the currect date_format parameter for the chosen granularity
	 */
    private String oracleDateFormat(StatisticsGranularity granularity) {
        switch (granularity) {
            case YEAR:
                return "YYYY";
            case MONTH:
                return "YYYY-MM";
            case DAY:
                return "YYYY-MM-DD";
            case HOUR:
                return "YYYY-MM-DDT%HH24";
            default:
                throw new IllegalArgumentException("Unable to convert " + granularity + " into a Oracle date format string");
        }
    }

}
