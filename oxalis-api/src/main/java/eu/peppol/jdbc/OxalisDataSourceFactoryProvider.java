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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ServiceLoader;

/**
 * Provides instances of the {@link OxalisDataSourceFactoryProvider} by using the service located design pattern.
 * This implementation uses the typical Java idiom of META-INF/services.
 *
 * Singleton instance, which is thread safe.
 *
 * User: steinar
 * Date: 08.02.13
 * Time: 17:03
 */
public class OxalisDataSourceFactoryProvider {

    private static final Logger log = LoggerFactory.getLogger(OxalisDataSourceFactoryProvider.class);

    private static class OxalisDataSourceFactoryHolder {
        private static final OxalisDataSourceFactory INSTANCE = OxalisDataSourceFactoryProvider.loadInstance();
    }


    /**
     * Singleton, lazy loaded, thread safe implementation, i.e. will always return the same OxalisDataSourceFactory
     *
     * @return
     */
    public static OxalisDataSourceFactory getInstance() {
        return OxalisDataSourceFactoryHolder.INSTANCE;
    }

    /**
     * Locates implementations of the OxalisDataSourceFactory using the META-INF/services idiom.
     * This method requires a little heavy lifting.
     *
     * @return a new instance of the OxalisDataSourceFactory
     * @see #getInstance() for an optimal solution in terms of performance.
     */
    public static OxalisDataSourceFactory loadInstance() {
        log.debug("Loading instance of " + OxalisDataSourceFactory.class.getName() + " from class path using META-INF/services idiom");

        try {
            // Locates the implementation by locating and reading the contents of text file
            // META-INF/services/eu.peppol.jdbc.OxalisDataSourceFactory
            String resourceName = "META-INF/services/" + OxalisDataSourceFactory.class.getName();
            log.debug("Looking for " + resourceName + " in classpath ...");
            URL resource = OxalisDataSourceFactoryProvider.class.getClassLoader().getResource(resourceName);
            if (resource != null) {
                log.debug("Found it in " + resource.toExternalForm());
            }   else {
                log.debug("Resource not found!");
            }

            ServiceLoader<OxalisDataSourceFactory> serviceLoader = ServiceLoader.load(OxalisDataSourceFactory.class);

            // No support for multiple implementations, the first one is picked.
            OxalisDataSourceFactory dataSourceFactory = serviceLoader.iterator().next();
            if (dataSourceFactory != null) {
                return dataSourceFactory;
            } else
                throw new IllegalStateException("Unable to load implementation of " + OxalisDataSourceFactory.class.getName() + " via META-INF/services");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load an implementation of " + OxalisDataSourceFactory.class.getName() + ". \nVerify that oxalis-jdbc-dbcp or oxalis-jdbc-jndi is on your class path");
        }
    }
}
