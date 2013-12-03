package eu.peppol.smp;

/**
 * Created with IntelliJ IDEA.
 * User: ebe
 * Date: 03.12.13
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */
public class SmlHost {
    public static final SmlHost PRODUCTION_SML = new SmlHost("sml.peppolcentral.org");
    public static final SmlHost TEST_SML = new SmlHost("smk.peppolcentral.org");

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
