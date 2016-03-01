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

package eu.peppol.jdbc;

import eu.peppol.util.GlobalConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Provides an instance of {@link javax.sql.DataSource} using the condfiguration parameters found
 * in {@link GlobalConfigurationImpl#OXALIS_GLOBAL_PROPERTIES_FILE_NAME}, which is located in
 * OXALIS_HOME.
 *
 * @author steinar
 *         Date: 18.04.13
 *         Time: 13:28
 */
public class OxalisDataSourceFactoryJndiImpl implements OxalisDataSourceFactory {


    public static final Logger log = LoggerFactory.getLogger(OxalisDataSourceFactoryJndiImpl.class);


    @Override
    public DataSource getDataSource() {
        String dataSourceJndiName = GlobalConfigurationImpl.getInstance().getDataSourceJndiName();

        log.debug("Obtaining data source from JNDI: " + dataSourceJndiName);
        try {
            Context initCtx = new InitialContext();

            return (DataSource) initCtx.lookup(dataSourceJndiName);
        } catch (NamingException e) {
            throw new IllegalStateException("Unable to obtain JNDI datasource from " + dataSourceJndiName + "; "+ e, e);
        }
    }
}
