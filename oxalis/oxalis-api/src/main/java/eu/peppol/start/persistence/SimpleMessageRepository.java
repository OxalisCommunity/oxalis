package eu.peppol.start.persistence;

import eu.peppol.start.util.IdentifierName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.Map;

/**
 * @author $Author$ (of last change)
 *         Created by
 *         User: steinar
 *         Date: 28.11.11
 *         Time: 21:09
 */
public class SimpleMessageRepository implements MessageRepository {

    public static final String SIMPLE_MESSAGE_REPOSITORY_CLASS_SAYS_HELLO_WORLD = "SimpleMessageRepository class says, hello world!";

    private static final Logger log = LoggerFactory.getLogger(SimpleMessageRepository.class);
    @Override
    public String toString() {
        return SIMPLE_MESSAGE_REPOSITORY_CLASS_SAYS_HELLO_WORLD;
    }

    @Override
    public void saveMessage(Map<IdentifierName,String> properties, Document document) {
        log.error("NO IMPLEMENTATION in " + this.getClass().getCanonicalName());

        for (Map.Entry<IdentifierName, String> e : properties.entrySet()) {
            log.debug(e.getKey() + ":" + e.getValue());
        }
    }
}
