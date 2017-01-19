package eu.peppol.outbound.lookup;

import com.google.inject.Inject;
import com.google.inject.Provider;
import no.difi.vefa.peppol.lookup.fetcher.BasicApacheFetcher;
import no.difi.vefa.peppol.mode.Mode;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * @author erlend
 * @since 4.0.0
 */
class OxalisApacheFetcher extends BasicApacheFetcher {

    private Provider<CloseableHttpClient> httpClientProvider;

    @Inject
    public OxalisApacheFetcher(Provider<CloseableHttpClient> httpClientProvider, Mode mode) {
        super(mode);
        this.httpClientProvider = httpClientProvider;
    }

    @Override
    protected CloseableHttpClient createClient() {
        return httpClientProvider.get();
    }
}
