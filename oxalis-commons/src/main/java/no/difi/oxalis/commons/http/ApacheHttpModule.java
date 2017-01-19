package no.difi.oxalis.commons.http;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import eu.peppol.util.OxalisVersion;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.net.ProxySelector;

/**
 * @author erlend
 * @since 4.0.0
 */
public class ApacheHttpModule extends AbstractModule {

    @Override
    protected void configure() {
        // No action.
    }

    @Provides
    @Singleton
    protected PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        httpClientConnectionManager.setDefaultMaxPerRoute(10);

        return httpClientConnectionManager;
    }

    @Provides
    @Singleton
    protected HttpRoutePlanner getHttpRoutePlanner() {
        // "SSLv3" is disabled by default : http://www.apache.org/dist/httpcomponents/httpclient/RELEASE_NOTES-4.3.x.txt
        return new SystemDefaultRoutePlanner(ProxySelector.getDefault());
    }

    @Provides
    protected CloseableHttpClient getHttpClient(PoolingHttpClientConnectionManager connectionManager,
                                                HttpRoutePlanner routePlanner) {
        return HttpClients.custom()
                .setUserAgent(String.format("Oxalis %s", OxalisVersion.getVersion()))
                        .setConnectionManager(connectionManager)
                        .setConnectionManagerShared(true)
                        .setRoutePlanner(routePlanner)
                        .build();
    }
}
