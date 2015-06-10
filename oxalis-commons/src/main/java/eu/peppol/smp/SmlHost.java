package eu.peppol.smp;

/**
 * Holds the default values for known SML lookup servers.
 *
 * Can be overridden by using oxalis-global.properties :
 * oxalis.sml.hostname=sml.peppolcentral.org
 *
 * @author ebe
 * @author thjo
 */
public class SmlHost {

    public static final SmlHost PRODUCTION_SML = new SmlHost("edelivery.tech.ec.europa.eu");
    public static final SmlHost TEST_SML = new SmlHost("acc.edelivery.tech.ec.europa.eu");

    private String hostname;

    public static SmlHost valueOf(String hostname) {
        return new SmlHost(hostname);
    }

    public SmlHost(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return hostname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmlHost smlHost = (SmlHost) o;
        if (!hostname.equals(smlHost.hostname)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return hostname.hashCode();
    }

}
