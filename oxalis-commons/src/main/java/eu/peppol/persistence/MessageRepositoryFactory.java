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

package eu.peppol.persistence;

import com.google.inject.Inject;
import eu.peppol.util.GlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Singleton implementation of a message repository factory.
 *
 * Will locate an implementation of {@link MessageRepository using the META-INF/services idiom, combined
 * with the optional value of {@link GlobalConfiguration#getPersistenceClassPath()}
 *
 * @author Steinar
 *         <p>
 *         Date: 28.11.11
 *         Time: 21:00
 */
public class MessageRepositoryFactory {

    private static final Logger log = LoggerFactory.getLogger(MessageRepositoryFactory.class);
    private final GlobalConfiguration globalConfiguration;


    @Inject
    public MessageRepositoryFactory(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }

    /**
     * Creates a ServiceLoader and attempts to load a custom implementation of MessageRepository.
     * If custom implementations are not available, the simple default file based repository is used.
     *
     * @return an implementation MessageRepository
     */
    public MessageRepository getInstanceWithDefault() {


        ServiceLoader<MessageRepository> serviceLoader = createServiceLoader();

        List<MessageRepository> messageRepositoryImplementations = new ArrayList<MessageRepository>();
        for (MessageRepository messageRepository : serviceLoader) {
            messageRepositoryImplementations.add(messageRepository);
        }

        // Uses the default SimpleMessageRepository if no other implementations could be found
        if (messageRepositoryImplementations.isEmpty()) {
            log.warn("No custom implementation of MessageRepository found, reverting to SimpleMessageRepository.");
            return new SimpleMessageRepository(new File(globalConfiguration.getInboundMessageStore()));
        }

        // Ah, found one or more implementations in the class path using the META-INF/services idiom
        if (messageRepositoryImplementations.size() > 1) {
            log.warn("Found " + messageRepositoryImplementations.size() + " implementations of " + MessageRepository.class);
        }

        // Provides the first available implementation
        return messageRepositoryImplementations.get(0);
    }

    /**
     * Inspects the configuration file <code>oxalis-global.properties</code>. If the property
     * <code>oxalis.persistence.class.path</code> has been set, a custom class loader is created and used when creating the
     * ServiceLoader. If this property is not given, a default ServiceLoader is created, which will use the current
     * context class loader.
     *
     * @return an initialized ServiceLoader instance
     */
    ServiceLoader<MessageRepository> createServiceLoader() {

        ServiceLoader<MessageRepository> serviceLoader = null;

        String path = globalConfiguration.getPersistenceClassPath();
        if (path != null && path.trim().length() > 0) {
            log.info("Attempting to create custom service loader based upon persistence class path set in oxalis-global.properties: " + path);
            serviceLoader = createCustomServiceLoader(path.trim());
        } else {
            serviceLoader = ServiceLoader.load(MessageRepository.class);
        }

        return serviceLoader;
    }

    /**
     * Creates a ServiceLoader, which will load implementations of MessageRepository using the
     * META-INF/services paradigm from a separate class loader, thus
     * enabling us to load the pluggable persistence module without reverting to a directory which is shared by everybody.
     *
     * @param persistenceClassPath the class path from which the MessageRepository implementation should be loaded from.
     * @return
     */
    static ServiceLoader<MessageRepository> createCustomServiceLoader(String persistenceClassPath) {

        if (persistenceClassPath == null || persistenceClassPath.trim().length() == 0) {
            throw new IllegalArgumentException("persistence class path null or empty");
        }
        if (!persistenceClassPath.endsWith("/") && !persistenceClassPath.endsWith(".jar")) {
            throw new IllegalStateException("Invalid class path: " + persistenceClassPath + " ; must end with either / or .jar");
        }

        try {
            URL classPathUrl = new URL(persistenceClassPath);
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{classPathUrl}, Thread.currentThread().getContextClassLoader());
            log.debug("Custom class loader: " + urlClassLoader.getURLs());

            URL r = urlClassLoader.getResource("META-INF/services/" + MessageRepository.class.getName());
            if (r == null) {
                log.warn("No META-INF/services file found for " + MessageRepository.class.getName());
            }

            ServiceLoader<MessageRepository> serviceLoader = ServiceLoader.load(MessageRepository.class, urlClassLoader);
            log.info("Loading MessageRepository instances from " + classPathUrl.toExternalForm());
            return serviceLoader;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Unable to establish class loader for path " + persistenceClassPath + "; " + e, e);
        }
    }
}
