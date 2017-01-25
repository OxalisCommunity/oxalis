/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package eu.peppol.persistence.jdbc;

import eu.peppol.persistence.guice.TestModuleFactory;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.RawStatisticsRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.sql.DataSource;

import static org.testng.Assert.assertNotNull;

/**
 * Various tests of the built-in versions of OxalisDataSourceFactory and RawStatisticsRepositoryFactory
 * @author steinar
 * @author thore
 */

@Guice(moduleFactory = TestModuleFactory.class)
@Test
public class RawStatisticsRepositoryJbcImplTest {

    public static final String CREATE_OXALIS_DBMS_H2_SQL = "sql/create-oxalis-dbms-h2.sql";

    public static final Logger log = LoggerFactory.getLogger(RawStatisticsRepositoryJbcImplTest.class);

    @Inject
    DataSource dataSource;

    @Inject
    RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory;

    @Test
    public void fetchRawStatisticsRepositoryTest() {
        RawStatisticsRepository instanceForRawStatistics = rawStatisticsRepositoryFactory.getInstanceForRawStatistics();
        assertNotNull(instanceForRawStatistics);

    }


}
