package eu.peppol.start.repository;

import eu.peppol.start.util.Log;

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

    public static ServiceLoader<MessageRepository> messageRepositoryServiceLoader = ServiceLoader.load(MessageRepository.class);

    /** Prevents any attempts to create instances of this class */
    private MessageRepositoryFactory(){}

    private enum MessageRepositorySingleton {
        INSTANCE;

        MessageRepository messageRepository;

        private MessageRepositorySingleton(){
            messageRepository = getInstanceWithDefault();
        }
    }

    /** Provides a singleton instance of MessageRepository */
    public static MessageRepository getInstance() {
        return MessageRepositorySingleton.INSTANCE.messageRepository;
    }

    
    /**
     * Attempts to get an instance of the message repository, throwing an exception if
     * an implementation could not be found in any META-INF/service/....MessageRepository
     *
     * @return instance of MessageRepository
     */
    public static MessageRepository getInstanceNoDefault() {
        MessageRepository messageRepository = getInstance();
        if (getInstance() == null){
            throw new IllegalStateException("No implementation of " + MessageRepository.class.getCanonicalName() + " found in class path. Searched for files matching /META-INF/services/" + MessageRepository.class.getCanonicalName() + " in class path");
        }

        return messageRepository;
    }


    static MessageRepository getInstanceWithDefault() {
        List<MessageRepository> messageRepositoryImplementations = new ArrayList<MessageRepository>();
        for (MessageRepository messageRepository : messageRepositoryServiceLoader) {
            messageRepositoryImplementations.add(messageRepository);
        }

        if (messageRepositoryImplementations.isEmpty()) {
            return new SimpleMessageRepository();
        }

        if (messageRepositoryImplementations.size() > 1) {
            Log.warn("Found " + messageRepositoryImplementations.size() + " implementations of " + MessageRepository.class);
        }

        // Provides the first available implementation
        return messageRepositoryImplementations.get(0);

    }
}
