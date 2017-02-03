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
import no.difi.oxalis.api.config.Settings;
import no.difi.oxalis.commons.config.builder.SettingsBuilder;
import no.difi.oxalis.commons.util.OxalisVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * @author erlend
 * @since 4.0.0
 */
public class ApacheHttpModule extends AbstractModule {

    private static final String USER_AGENT = String.format("Oxalis %s", OxalisVersion.getVersion());

    @Override
    protected void configure() {
        // No action.
        SettingsBuilder.with(binder(), HttpConf.class, "HTTP")
                .add(HttpConf.POOL_MAX_ROUTE, "http.pool.max_route")
                .add(HttpConf.POOL_TOTAL, "http.pool.total")
                .add(HttpConf.TIMEOUT_CONNECT, "http.timeout.connect")
                .add(HttpConf.TIMEOUT_READ, "http.timeout.read");
    }

    @Provides
    @Singleton
    protected PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager(Settings<HttpConf> settings) {
        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        httpClientConnectionManager.setDefaultMaxPerRoute(settings.getInt(HttpConf.POOL_MAX_ROUTE));
        httpClientConnectionManager.setMaxTotal(settings.getInt(HttpConf.POOL_TOTAL));

        return httpClientConnectionManager;
    }

    @Provides
    @Singleton
    protected RequestConfig getRequestConfig(Settings<HttpConf> settings) {
        return RequestConfig.custom()
                .setConnectTimeout(settings.getInt(HttpConf.TIMEOUT_CONNECT))
                .setConnectionRequestTimeout(settings.getInt(HttpConf.TIMEOUT_READ))
                .build();
    }

    @Provides
    protected CloseableHttpClient getHttpClient(PoolingHttpClientConnectionManager connectionManager,
                                                RequestConfig requestConfig) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setUserAgent(USER_AGENT);

        // Request configuration
        httpClientBuilder.setDefaultRequestConfig(requestConfig);

        // Connection pool
        httpClientBuilder.setConnectionManager(connectionManager);
        httpClientBuilder.setConnectionManagerShared(true);

        // Use system default for proxy
        httpClientBuilder.useSystemProperties();

        return httpClientBuilder.build();
    }
}
