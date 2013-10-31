package eu.peppol.smp;

import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;

import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @author steinar
 *         Date: 29.10.13
 *         Time: 16:23
 */
public interface SmpLookupManager {

    URL getEndpointAddress(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier);

    X509Certificate getEndpointCertificate(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier);

    List<PeppolDocumentTypeId> getServiceGroups(ParticipantId participantId) throws SmpLookupException, ParticipantNotRegisteredException;

    /**
     * Provides information about the end point for the combination of a PEPPOL participant identifier and
     * document type identifier.
     *
     * @param participantId
     * @param documentTypeIdentifier
     * @return
     */
    PeppolEndpointData getEndpointData(ParticipantId participantId, PeppolDocumentTypeId documentTypeIdentifier);

    public static class PeppolEndpointData {
        URL url;
        BusDoxProtocol busDoxProtocol;

        public PeppolEndpointData(URL url, BusDoxProtocol busDoxProtocol) {
            this.url = url;
            this.busDoxProtocol = busDoxProtocol;
        }

        public URL getUrl() {
            return url;
        }

        public BusDoxProtocol getBusDoxProtocol() {
            return busDoxProtocol;
        }
    }
}
