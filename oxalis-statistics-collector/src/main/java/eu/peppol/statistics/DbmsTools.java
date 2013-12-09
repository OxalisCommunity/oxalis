package eu.peppol.statistics;

/**
 * @author steinar
 *         Date: 03.04.13
 *         Time: 21:29
 */
class DbmsTools {


    void createDatabaseSchema() {
        RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory = RawStatisticsRepositoryFactoryProvider.getInstance();

        // TODO: needs to be implemented
/*
        AggregatedStatisticsRepository aggregatedStatisticsRepository = rawStatisticsRepositoryFactory.getInstanceForAggregatedStatistics();
        aggregatedStatisticsRepository.createDatabaseSchemaForDataWarehouse();
*/
    }
}

