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

import com.google.inject.CreationException;
import com.google.inject.Guice;
import network.oxalis.statistics.guice.RawStatisticsRepositoryModule;
import org.testng.annotations.Test;

import static org.testng.Assert.fail;

/**
 * @author steinar
 *         Date: 28.10.2016
 *         Time: 08.38
 */
public class RawStatisticsRepositoryModuleTest {

    @Test
    public void testConfigure() throws Exception {

        try {
            Guice.createInjector(new RawStatisticsRepositoryModule());
            fail("Should not be able to create injector with supplying a module providing \n" +
                    "an instance of  DataSource and Settings");
        } catch (CreationException e) {
            // As expected
        }
    }

}
