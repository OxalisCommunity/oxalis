/* Created by steinar on 18.05.12 at 13:35 */
package eu.peppol.smp;

import eu.peppol.start.identifier.ParticipantId;

import java.net.URL;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class SmpLookupException extends Throwable {
    ParticipantId participantId;
    private URL url;

    public SmpLookupException(ParticipantId participantId, Exception e) {
        super("Unable to perform SMP lookup for " + participantId + "; " + e.getMessage(), e);
        this.participantId = participantId;
    }

    public SmpLookupException(ParticipantId participantId, URL servicesUrl, Exception cause) {
        super("Unable to fetch data for " + participantId + " from " + servicesUrl + " ;" + cause.getMessage(), cause);
        this.participantId = participantId;
        this.url = servicesUrl;
    }

    public ParticipantId getParticipantId() {
        return participantId;
    }

    public URL getSmpUrl() {
        return url;
    }
}
