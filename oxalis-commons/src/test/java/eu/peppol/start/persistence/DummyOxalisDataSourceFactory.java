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

package eu.peppol.start.persistence;

import eu.peppol.jdbc.OxalisDataSourceFactory;

import javax.sql.DataSource;

/**
 * A dummy OxalisDataSourceFactory, which is used for testing purposes.
 *
 * @author steinar
 *         Date: 02.12.13
 *         Time: 19:30
 */
public class DummyOxalisDataSourceFactory implements OxalisDataSourceFactory {
    @Override
    public DataSource getDataSource() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
