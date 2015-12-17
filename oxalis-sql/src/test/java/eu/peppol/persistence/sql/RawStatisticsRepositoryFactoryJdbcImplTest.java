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

import eu.peppol.jdbc.OxalisDataSourceFactory;
import eu.peppol.jdbc.OxalisDataSourceFactoryProvider;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.RawStatisticsRepositoryFactory;
import eu.peppol.statistics.RawStatisticsRepositoryFactoryProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import static org.testng.Assert.*;

/**
 * Various tests of the built-in versions of OxalisDataSourceFactory and RawStatisticsRepositoryFactory
 * @author steinar
 * @author thore
 */
@Test(groups = "integration")
public class RawStatisticsRepositoryFactoryJdbcImplTest {

    @Test
    public void oxalisDataSourceFactoryIsSingleton() throws Exception {

        // Attempts to load the first instance of OxalisDataSourceFactory
        OxalisDataSourceFactory oxalisDataSourceFactory = OxalisDataSourceFactoryProvider.getInstance();
        assertNotNull(oxalisDataSourceFactory);

        // Second invocation should return same instance
        OxalisDataSourceFactory oxalisDataSourceFactory2 = OxalisDataSourceFactoryProvider.getInstance();
        assertEquals(oxalisDataSourceFactory, oxalisDataSourceFactory2, "Seems the Singletong pattern in OxalisDataSourceFactoryProvider is not working");

        // The datasource should also be the same instance
        DataSource dataSource1 = oxalisDataSourceFactory.getDataSource();
        assertNotNull(dataSource1);
        DataSource dataSource2 = oxalisDataSourceFactory.getDataSource();
        assertEquals(dataSource1, dataSource2, OxalisDataSourceFactory.class.getSimpleName() + " is not returning a singleton instance of DataSource");

    }

    @Test
    public void statisticsRepositoryFactoryIsSingleton() {

        // Verifies that the StatisticsRepositoryFactoryProvider returns a singleton instance
        RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory = RawStatisticsRepositoryFactoryProvider.getInstance();
        assertNotNull(rawStatisticsRepositoryFactory);
        RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory2 = RawStatisticsRepositoryFactoryProvider.getInstance();
        assertEquals(rawStatisticsRepositoryFactory, rawStatisticsRepositoryFactory2, "Singleton pattern brok in " + RawStatisticsRepositoryFactoryProvider.class.getSimpleName());

        // However; the StatisticsRepository instances should not be singleton
        RawStatisticsRepository rawStatisticsRepository = rawStatisticsRepositoryFactory.getInstanceForRawStatistics();
        assertNotNull(rawStatisticsRepository);
        RawStatisticsRepository rawStatisticsRepository2 = rawStatisticsRepositoryFactory.getInstanceForRawStatistics();
        assertNotEquals(rawStatisticsRepository, rawStatisticsRepository2, "StatisticsRepositoryFactory.getInstanceForRawStatistics() should not produce singleton instance");

    }

    @Test
    public void makeSureWeLoadTheBuiltInServices() {

        // Make sure we get the default built-in version of the OxalisDataSourceFactory
        OxalisDataSourceFactory oxalisDataSourceFactory = OxalisDataSourceFactoryProvider.getInstance();
        assertEquals(oxalisDataSourceFactory.getClass().getName(), "eu.peppol.jdbc.OxalisDataSourceFactoryDbcpImpl", "Got unexpected instance in return, expected eu.peppol.jdbc.OxalisDataSourceFactoryDbcpImpl");

        // Make sure we get the default built-in version of the OxalisDataSourceFactory and RawStatisticsRepositoryFactory
        RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory = RawStatisticsRepositoryFactoryProvider.getInstance();
        assertEquals(rawStatisticsRepositoryFactory.getClass().getName(), "eu.peppol.persistence.sql.RawStatisticsRepositoryFactoryJdbcImpl", "Got unexpected instance in return, expected eu.peppol.persistence.sql.RawStatisticsRepositoryFactoryJdbcImpl");

    }

}
