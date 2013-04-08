package eu.peppol.statistics;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Objects implementing this interface are capable of storing and retrieving raw data pertaining to the sending and
 * receiving of PEPPOL messages in order to provide statistics.
 * <p/>
 * User: steinar
 * Date: 30.01.13
 * Time: 19:28
 */
public interface StatisticsRepository {

    DataSource getDataSource();

    /** Creates the database scheme for the data warehouse */
    void createDatabaseSchemaForDataWarehouse();

    /** Persists another raw statistics entry into table {@code raw_stats} */
    Integer persist(RawStatistics rawStatistics);


    /**
     * Retrieves data from table raw_stats and transforms it into an appropriate XML document
     *
     */
    void fetchAndTransform(StatisticsTransformer transformer, Date start, Date end, StatisticsGranularity granularity);

    /** Persists an aggregated statistics entry into the data warehouse star scheme model */
    Integer persist(AggregatedStatistics statisticsEntry);
}
