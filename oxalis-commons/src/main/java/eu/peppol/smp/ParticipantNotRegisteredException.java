/* Created by steinar on 18.05.12 at 13:35 */
package eu.peppol.smp;

import eu.peppol.identifier.ParticipantId;

/**
 * To be thrown when participant is not registered
 */
public class ParticipantNotRegisteredException extends Exception {

    ParticipantId participantId;

    public ParticipantNotRegisteredException(ParticipantId participantId) {
        this.participantId = participantId;
    }

    public ParticipantId getParticipantId() {
        return participantId;
    }
}
