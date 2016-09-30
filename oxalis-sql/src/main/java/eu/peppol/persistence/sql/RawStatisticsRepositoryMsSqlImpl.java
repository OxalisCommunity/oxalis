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

import eu.peppol.statistics.StatisticsGranularity;

import javax.sql.DataSource;

/**
 * This is RawStatisticsRepository implementation for running the statistics database on MsSql backend, through Jdbc.
 *
 * User: zeko78
 * Date: 07.11.14
 * Time: 11:54
 */
public class RawStatisticsRepositoryMsSqlImpl extends RawStatisticsRepositoryJdbcImpl {

    public RawStatisticsRepositoryMsSqlImpl(DataSource dataSource) {
		super(dataSource);
    }

	/**
 	 * Composes the SQL query to persist raw statistics into the DBMS.
	 *
	 * @return
	 */
	String getPersistSqlQueryText() {
		return String.format("INSERT INTO %s (ap, tstamp,  direction, sender, receiver, doc_type, profile, channel) values(?,?,?,?,?,?,?,?)", RAW_STATS_TABLE_NAME);
	}

	/**
	 * Composes the SQL query for retrieval of statistical data between a start and end data, with
	 * a granularity as supplied.
	 *
	 * @param granularity the granularity of the statics period reported.
	 * @return
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
	 * @return
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
                throw new IllegalArgumentException("Unable to convert " + granularity + " into a MsSQL function string");
        }
    }

}
