/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.commons.http;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import no.difi.oxalis.commons.util.OxalisVersion;
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
