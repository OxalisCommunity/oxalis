package eu.peppol.statistics;

import java.util.Date;

/**
 * Objects implementing this interface are capable of storing and retrieving raw data
 * pertaining to the sending and receiving of PEPPOL messages in order to provide statistics.
 *
 * User: steinar
 * Date: 30.01.13
 * Time: 19:28
 */
public interface RawStatisticsRepository {

    /**
     * Persists another raw statistics entry into table {@code raw_stats}
     * */
    Integer persist(RawStatistics rawStatistics);

    /**
     * Retrieves data from table <code>raw_stats</code> and transforms it into an appropriate XML document
     */
    void fetchAndTransformRawStatistics(StatisticsTransformer transformer, Date start, Date end, StatisticsGranularity granularity);

}
