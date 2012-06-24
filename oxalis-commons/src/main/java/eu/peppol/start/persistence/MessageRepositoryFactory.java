package eu.peppol.start.persistence;

import eu.peppol.start.identifier.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author $Author$ (of last change)
 *         Created by
 *         User: steinar
 *         Date: 28.11.11
 *         Time: 21:00
 */
public class MessageRepositoryFactory {

//    public static ServiceLoader<MessageRepository> messageRepositoryServiceLoader = ServiceLoader.load(MessageRepository.class);

    private static final Logger log = LoggerFactory.getLogger(MessageRepositoryFactory.class);

    /**
     * Prevents any attempts to create instances of this class
     */
    private MessageRepositoryFactory() {
    }

    private enum MessageRepositorySingleton {
        INSTANCE;

        MessageRepository messageRepository;

        private MessageRepositorySingleton() {
            messageRepository = getInstanceWithDefault();
        }
    }


    /**
     * Provides a singleton instance of MessageRepository
     */
    public static MessageRepository getInstance() {
        return MessageRepositorySingleton.INSTANCE.messageRepository;
    }


    /**
     * Attempts to get an instance of the message persistence, throwing an exception if
     * an implementation could not be found in any META-INF/service/....MessageRepository
     *
     * @return instance of MessageRepository
     */
    public static MessageRepository getInstanceNoDefault() {
        MessageRepository messageRepository = getInstance();
        if (getInstance() == null) {
            throw new IllegalStateException("No implementation of " + MessageRepository.class.getCanonicalName() + " found in class path. Searched for files matching /META-INF/services/" + MessageRepository.class.getCanonicalName() + " in class path");
        }

        return messageRepository;
    }


    /**
     * Creates a ServiceLoader and attempts to load a custom implementation of MessageRepository.
     * If custom implementations are not available, the simple default file based repository is used.
     *
     * @return an implementation MessageRepository
     */
    static MessageRepository getInstanceWithDefault() {


        ServiceLoader<MessageRepository> serviceLoader = createServiceLoader();

        List<MessageRepository> messageRepositoryImplementations = new ArrayList<MessageRepository>();
        for (MessageRepository messageRepository : serviceLoader) {
            messageRepositoryImplementations.add(messageRepository);
        }

        if (messageRepositoryImplementations.isEmpty()) {
            log.warn("No custom implementation of MessageFactory found, reverting to SimpleMessageRepository.");
            return new SimpleMessageRepository();
        }

        if (messageRepositoryImplementations.size() > 1) {
            log.warn("Found " + messageRepositoryImplementations.size() + " implementations of " + MessageRepository.class);
        }

        // Provides the first available implementation
        return messageRepositoryImplementations.get(0);
    }


    /**
     * Inspects the configuration file <code>oxalis-web.properties</code>. If the property
     * <code>oxalis.persistence.class.path</code> has been set, a custom class loader is created and used when creating the
     * ServiceLoader. If this property is not given, a default ServiceLoader is created, which will use the current
     * context class loader.
     *
     * @return an initialized ServiceLoader instance
     */
    static ServiceLoader<MessageRepository> createServiceLoader() {

        ServiceLoader<MessageRepository> serviceLoader = null;

        String path = Configuration.getInstance().getPersistenceClassPath();
        if (path != null && path.trim().length() > 0) {
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
                log.error("No META-INF/services file found for " + MessageRepository.class.getName());
            }

            ServiceLoader<MessageRepository> serviceLoader = ServiceLoader.load(MessageRepository.class, urlClassLoader);
            log.info("Loading MessageRepository instances from " + classPathUrl.toExternalForm());
            return serviceLoader;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Unable to establish class loader for path " + persistenceClassPath + "; " + e, e);
        }
    }
}
