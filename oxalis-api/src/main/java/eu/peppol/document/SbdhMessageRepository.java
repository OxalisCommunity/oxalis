package eu.peppol.document;

import eu.peppol.PeppolMessageMetaData;

import java.io.InputStream;

/**
 * Represents a repository for messages received, which are wrapped in an SBDH (Standard Business Document Header).
 *
 * This stuff was introduced together with the AS2 protocol.
 *
 * You might wonder why this stuff was not incorporated into the {@link eu.peppol.start.persistence.MessageRepository} interface?
 * The answer is: if it works, don't mess with it.
 *
 *
 * @author steinar
 *         Date: 24.10.13
 *         Time: 10:04
 */
public interface SbdhMessageRepository {

    void persist(PeppolMessageMetaData transmissionData, InputStream payload) throws SbdhMessageException;
}
