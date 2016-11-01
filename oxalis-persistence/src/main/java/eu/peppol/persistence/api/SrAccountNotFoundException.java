package eu.peppol.persistence.api;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.persistence.api.account.AccountId;

/**
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 31.12.11
 *         Time: 17:19
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
