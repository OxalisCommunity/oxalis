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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Provides instances of the {@link OxalisDataSourceFactoryProvider} by using the service located design pattern.
 * This implementation uses the typical Java idiom of META-INF/services.
 * <p>
 * Singleton instance, which is thread safe.
 *
 * @author steinar
 *         Date: 08.02.13
 *         Time: 17:03
 */
public class OxalisDataSourceFactoryProvider {

    private static final Logger log = LoggerFactory.getLogger(OxalisDataSourceFactoryProvider.class);

    /**
     * Singleton, lazy loaded, thread safe implementation, i.e. will always return the same OxalisDataSourceFactory
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
    public static synchronized OxalisDataSourceFactory loadInstance() {

        // Attempts to load the JNDI based one first if it fails, revert to our DBCP implementation
        log.debug("Loading instance of '{}' from class path using META-INF/services idiom.",
                OxalisDataSourceFactory.class.getName());

        try {
            // Locates the implementation by locating and reading the contents of text file (resource in class path)
            // META-INF/services/eu.peppol.jdbc.OxalisDataSourceFactory
            verifyExistenceInClassPath();

            // Locates all implementations in META-INF/services in classpath
            ServiceLoader<OxalisDataSourceFactory> serviceLoader = ServiceLoader.load(OxalisDataSourceFactory.class);

            // Handles situations with more than our default implementation
            List<OxalisDataSourceFactory> factoryImplementations = new ArrayList<>();
            for (OxalisDataSourceFactory oxalisDataSourceFactory : serviceLoader) {
                factoryImplementations.add(oxalisDataSourceFactory);
            }

            return chooseImplementationToUse(factoryImplementations);
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Unable to load an implementation of '%s'. " +
                            "Verify that oxalis-jdbc-dbcp or oxalis-jdbc-jndi is on your class path",
                    OxalisDataSourceFactory.class.getName()));
        }
    }

    protected static OxalisDataSourceFactory chooseImplementationToUse(
            List<OxalisDataSourceFactory> factoryImplementations) {

        OxalisDataSourceFactory chosenImplementation = null;

        // Handles situations with one, two or multiple implementations of the OxalisDataSourceFactory
        if (!factoryImplementations.isEmpty()) {
            // If there is only a single implementation found, it is the default supplied together with Oxalis
            if (factoryImplementations.size() == 1) {
                // Returns the only implementation found...
                chosenImplementation = factoryImplementations.get(0);

            } else
                // If there are two or more, choose first one, which is not our provided one.
                if (factoryImplementations.size() >= 2) {
                    for (OxalisDataSourceFactory oxalisDataSourceFactory : factoryImplementations) {
                        // Returns the implementation, which is meant to override ours
                        if (!oxalisDataSourceFactory.isProvidedWithOxalisDistribution()) {
                            chosenImplementation = oxalisDataSourceFactory;
                        }
                    }
                }
        } else
            throw new IllegalStateException(String.format(
                    "Unable to load implementation of '%s' via META-INF/services",
                    OxalisDataSourceFactory.class.getName()));

        return chosenImplementation;
    }

    protected static void verifyExistenceInClassPath() throws IOException {
        String resourceName = "META-INF/services/" + OxalisDataSourceFactory.class.getName();
        log.debug("Looking for " + resourceName + " in classpath ...");
        Enumeration<URL> resource = OxalisDataSourceFactoryProvider.class.getClassLoader().getResources(resourceName);
        if (resource.hasMoreElements()) {
            do {
                log.debug("Found implementation in " + resource.nextElement());
            } while (resource.hasMoreElements());
        } else {
            log.debug(resourceName + " not found in classpath, this is going to fail!");
        }
    }

    private static class OxalisDataSourceFactoryHolder {
        private static final OxalisDataSourceFactory INSTANCE = OxalisDataSourceFactoryProvider.loadInstance();
    }
}
