package eu.peppol.statistics;

import java.util.Date;

/**
 * @author steinar
 *         Date: 15.08.13
 *         Time: 16:03
 */
public interface AggregatedStatisticsRepository {

    /** Creates the database scheme for the data warehouse */
    void createDatabaseSchemaForDataWarehouse();


    /** Persists an aggregated statistics entry into the data warehouse star scheme model */
    Integer persist(AggregatedStatistics statisticsEntry);


    void selectAggregatedStatistics(ResultSetWriter resultSetWriter,Date start, Date end, StatisticsGranularity granularity);

    /**
     * Closes the repository, releasing any resources being used.
     */
    void close();

}
