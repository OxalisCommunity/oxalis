package eu.peppol.smp;

import eu.peppol.BusDoxProtocol;
import eu.peppol.security.CommonName;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.TransportProfile;

import java.net.URI;
import java.security.cert.X509Certificate;

@Deprecated
public class PeppolEndpointData {

    private URI url;

    private TransportProfile transportProfile;

    private X509Certificate certificate = null;

    private CommonName commonName = null;

    @Deprecated
    public PeppolEndpointData(URI url, BusDoxProtocol transportProfile, CommonName commonName) {
        this.url = url;
        this.transportProfile = transportProfile.toVefa();
        if (commonName != null) {
            this.commonName = commonName;
            this.certificate = commonName.getCertificate();
        }
    }

    public PeppolEndpointData(Endpoint endpoint) {
        this.url = endpoint.getAddress();
        this.transportProfile = endpoint.getTransportProfile();
        this.certificate = endpoint.getCertificate();
        if (certificate != null)
            this.commonName = CommonName.of(certificate);
    }

    public Endpoint toVefa() {
        return Endpoint.of(transportProfile, url, certificate);
    }

    public URI getUrl() {
        return url;
    }

    public TransportProfile getTransportProfile() {
        return transportProfile;
    }

    /**
     * The CN attribute of the Endpoint's X.509 Distinguished Name
     *
     * @return the value of the CN attribute or <code>null</code> if not set.
     */
    public CommonName getCommonName() {
        return commonName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PeppolEndpointData{");
        sb.append("url=").append(url);
        sb.append(", transportProfile=").append(transportProfile);
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
        if (transportProfile != null ? !transportProfile.equals(that.transportProfile) : that.transportProfile != null)
            return false;
        if (commonName != null ? !commonName.equals(that.commonName) : that.commonName != null) return false;
        return true;
    }
}
