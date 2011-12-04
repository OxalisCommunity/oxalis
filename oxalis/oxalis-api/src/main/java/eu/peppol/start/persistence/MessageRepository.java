package eu.peppol.start.persistence;

import eu.peppol.start.identifier.IdentifierName;
import eu.peppol.start.identifier.PeppolMessageHeader;
import org.w3c.dom.Document;

import java.util.Map;

/**
 * Repository of messages received and sent.
 * 
 * @author Steinar Overbeck Cook
 *
 *         Created by
 *         User: steinar
 *         Date: 28.11.11
 *         Time: 20:55
 */
public interface MessageRepository {


    /**
     * Saves the supplied message, using the given arguments.
     * @param inboundMessageStore the full path to the directory in which the inbound messages should be stored.
     * @param peppolMessageHeader represents the message headers used for routing
     * @param document represents the message received, which should be persisted.
     */
    public void saveInboundMessage(String inboundMessageStore, PeppolMessageHeader peppolMessageHeader, Document document);
    
}
