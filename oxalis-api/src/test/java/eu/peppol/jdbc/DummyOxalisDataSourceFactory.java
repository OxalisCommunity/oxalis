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

package eu.peppol.jdbc;

import javax.sql.DataSource;

/**
 * @author steinar
 *         Date: 19.10.2016
 *         Time: 18.14
 */
public class DummyOxalisDataSourceFactory implements OxalisDataSourceFactory {

    @Override
    public DataSource getDataSource() {
        return null;
    }


    @Override
    public boolean isProvidedWithOxalisDistribution() {
        // Returns true as we are the only one supplied as part of the tests
        return true;
    }
}
