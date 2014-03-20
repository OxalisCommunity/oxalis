package eu.peppol.smp;

import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.security.CommonName;
import org.busdox.smp.SignedServiceMetadataType;

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
     * Provides information about the end point for the combination of a
     * PEPPOL participant identifier and document type identifier.
     */
    PeppolEndpointData getEndpointTransmissionData(ParticipantId participantId, PeppolDocumentTypeId documentTypeIdentifier);

    SignedServiceMetadataType getServiceMetaData(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) throws SmpSignedServiceMetaDataException;

    public static class PeppolEndpointData {

        URL url;
        BusDoxProtocol busDoxProtocol;
        CommonName commonName = null;

        public PeppolEndpointData(URL url, BusDoxProtocol busDoxProtocol) {
            this.url = url;
            this.busDoxProtocol = busDoxProtocol;
        }

        public PeppolEndpointData(URL url, BusDoxProtocol busDoxProtocol, CommonName commonName) {
            this(url, busDoxProtocol);
            this.commonName = commonName;
        }

        public URL getUrl() {
            return url;
        }

        public BusDoxProtocol getBusDoxProtocol() {
            return busDoxProtocol;
        }

        /**
         * The CN attribute of the Endpoint's X.509 Distinguished Name
         * @return the value of the CN attribute or <code>null</code> if not set.
         */
        public CommonName getCommonName() {
            return commonName;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("PeppolEndpointData{");
            sb.append("url=").append(url.toExternalForm());
            sb.append(", busDoxProtocol=").append(busDoxProtocol);
            sb.append(", commonName=").append(commonName);
            sb.append('}');
            return sb.toString();
        }

    }

}
