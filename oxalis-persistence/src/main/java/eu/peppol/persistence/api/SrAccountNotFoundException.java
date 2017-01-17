package eu.peppol.persistence.api;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.persistence.AccountId;

/**
 * @author Steinar Overbeck Cook
 */
public class SrAccountNotFoundException extends Exception {

    public SrAccountNotFoundException(AccountId id) {
        super("SR Account " + id + " not found");
    }

    public SrAccountNotFoundException(ParticipantId participantId) {
        super("SR Account for participant id " + participantId + " not found");
    }

    public SrAccountNotFoundException(UserName username) {
        super("SR Account for username" + username + " not found");
    }
}
