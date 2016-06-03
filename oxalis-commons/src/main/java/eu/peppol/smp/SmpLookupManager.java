/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.smp;

import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.security.CommonName;
import org.busdox.servicemetadata.publishing._1.SignedServiceMetadataType;

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PeppolEndpointData that = (PeppolEndpointData) o;
            if (url != null ? !url.equals(that.url) : that.url != null) return false;
            if (busDoxProtocol != null ? !busDoxProtocol.equals(that.busDoxProtocol) : that.busDoxProtocol != null) return false;
            if (commonName != null ? !commonName.equals(that.commonName) : that.commonName != null) return false;
            return true;
        }

    }

}
