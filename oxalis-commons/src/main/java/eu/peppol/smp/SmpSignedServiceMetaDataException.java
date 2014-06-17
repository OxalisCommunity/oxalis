package eu.peppol.smp;

import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.ParticipantId;

import java.net.URL;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class SmpSignedServiceMetaDataException extends Exception {

    private final ParticipantId participant;
    private final PeppolDocumentTypeId documentTypeIdentifier;
    private final URL smpUrl;

    public SmpSignedServiceMetaDataException(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier, URL smpUrl, Exception e) {
        super("Unable to find information for participant: " + participant + ", documentType: " + documentTypeIdentifier + ", at url: " + smpUrl + " ; " + e.getMessage(), e);
        this.participant = participant;
        this.documentTypeIdentifier = documentTypeIdentifier;
        this.smpUrl = smpUrl;
    }

    public ParticipantId getParticipant() {
        return participant;
    }

    public PeppolDocumentTypeId getDocumentTypeIdentifier() {
        return documentTypeIdentifier;
    }

    public URL getSmpUrl() {
        return smpUrl;
    }

}
