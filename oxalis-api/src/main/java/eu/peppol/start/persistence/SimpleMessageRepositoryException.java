package eu.peppol.start.persistence;

import java.io.File;

/**
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 22:34
 */
public class SimpleMessageRepositoryException extends RuntimeException {

    public SimpleMessageRepositoryException(File outputFile, Exception e) {
        super("Unable to process " + outputFile + "; " + e, e);
    }
}
