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

package no.difi.oxalis.persistence.testng;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import no.difi.oxalis.persistence.guice.AopJdbcTxManagerModule;
import no.difi.oxalis.persistence.guice.DataSourceModule;
import no.difi.oxalis.commons.filesystem.FileSystemModule;
import no.difi.oxalis.test.config.TestConfigModule;
import org.testng.IModuleFactory;
import org.testng.ITestContext;

/**
 * TestNG implementation of factory, which provides the Google Guice modules required
 * for running the tests.
 *
 * @author steinar
 *         Date: 16.10.2016
 *         Time: 19.44
 */
public class PersistenceModuleFactory implements IModuleFactory {

    public static final String CREATE_OXALIS_DBMS_H2_SQL = "/sql/raw_stats-h2.sql";

    @Override
    public Module createModule(ITestContext iTestContext, Class<?> aClass) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                install(new DataSourceModule());
                install(new AopJdbcTxManagerModule());
                install(new TestConfigModule());
                install(new FileSystemModule());
            }
        };
    }
}
