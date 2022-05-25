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

import network.oxalis.persistence.annotation.Repository;
import network.oxalis.persistence.api.JdbcTxManager;
import network.oxalis.statistics.api.StatisticsGranularity;

import javax.inject.Inject;

/**
 * This is RawStatisticsRepository implementation for running the statistics database on MsSql backend, through Jdbc.
 *
 * @author zeko78
 */
@Repository
public class RawStatisticsRepositoryMsSqlImpl extends RawStatisticsRepositoryJdbcImpl {

    @Inject
    public RawStatisticsRepositoryMsSqlImpl(JdbcTxManager jdbcTxManager) {
        super(jdbcTxManager);
    }

    /**
     * Composes the SQL query to persist raw statistics into the DBMS.
     */
    String getPersistSqlQueryText() {
        return String.format("INSERT INTO %s (ap, tstamp,  direction, sender, receiver, doc_type, profile, channel) " +
                "values(?,?,?,?,?,?,?,?)", RawStatisticsRepositoryJdbcImpl.RAW_STATS_TABLE_NAME);
    }

    /**
     * Composes the SQL query for retrieval of statistical data between a start and end data, with
     * a granularity as supplied.
     *
     * @param granularity the granularity of the statics period reported.
     */
    String getRawStatisticsSqlQueryText(StatisticsGranularity granularity) {
        String granularityQuery = granularityQuery(granularity);
        String sql = "SELECT\n" +
                "  ap,\n" +
                "  'OUT' direction,\n" +
                "  " + granularityQuery + " period,\n" +
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
                "GROUP BY ap,direction," + granularityQuery + ",sender,doc_type,profile,channel\n" +
                "union\n" +
                "SELECT\n" +
                "  ap,\n" +
                "  'IN' direction,\n" +
                "  " + granularityQuery + " period,\n" +
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
                "GROUP BY ap,direction," + granularityQuery + ",receiver,doc_type,profile,channel\n" +
                "order by period, ap\n" +
                ";";

        return sql;
    }

    /**
     * Return the currect date_format parameter for the chosen granularity
     */
    static String granularityQuery(StatisticsGranularity granularity) {
        switch (granularity) {
            case YEAR:
                return "LEFT(CONVERT(VARCHAR, CONVERT(datetime, tstamp, 121), 121), 4)";
            case MONTH:
                return "LEFT(CONVERT(VARCHAR, CONVERT(datetime, tstamp, 121), 121), 7)";
            case DAY:
                return "LEFT(CONVERT(VARCHAR, CONVERT(datetime, tstamp, 121), 121), 10)";
            case HOUR:
                return "REPLACE(LEFT(CONVERT(VARCHAR, CONVERT(datetime, tstamp, 121), 121), 13), ' ', 'T')";
            default:
                throw new IllegalArgumentException(String.format(
                        "Unable to convert '%s' into a MsSQL function string", granularity));
        }
    }
}
