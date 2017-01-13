package eu.peppol.smp;

import eu.peppol.BusDoxProtocol;
import eu.peppol.security.CommonName;
import no.difi.vefa.peppol.common.model.Endpoint;

import java.net.MalformedURLException;
import java.net.URL;

public class PeppolEndpointData {

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

    public PeppolEndpointData(Endpoint endpoint) {
        try {
            url = new URL(endpoint.getAddress());
            busDoxProtocol = BusDoxProtocol.AS2;
            commonName = CommonName.valueOf(endpoint.getCertificate().getSubjectX500Principal());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

    }

    public URL getUrl() {
        return url;
    }

    public BusDoxProtocol getBusDoxProtocol() {
        return busDoxProtocol;
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
        if (busDoxProtocol != null ? !busDoxProtocol.equals(that.busDoxProtocol) : that.busDoxProtocol != null)
            return false;
        if (commonName != null ? !commonName.equals(that.commonName) : that.commonName != null) return false;
        return true;
    }

}
