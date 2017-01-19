package eu.peppol.outbound.lookup;

import com.google.inject.Inject;
import no.difi.vefa.peppol.lookup.fetcher.ApacheFetcher;
import no.difi.vefa.peppol.mode.Mode;

/**
 * @author erlend
 * @since 4.0.0
 */
class OxalisApacheFetcher extends ApacheFetcher {

    @Inject
    public OxalisApacheFetcher(Mode mode) {
        super(mode);
    }
}
