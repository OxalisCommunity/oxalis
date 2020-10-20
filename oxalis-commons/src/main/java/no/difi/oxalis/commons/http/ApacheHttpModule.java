/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.opentracing.Tracer;
import io.opentracing.contrib.apache.http.client.TracingHttpClientBuilder;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.commons.guice.OxalisModule;
import no.difi.oxalis.commons.util.OxalisVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.TimeUnit;

/**
 * @author erlend
 * @since 4.0.0
 */
public class ApacheHttpModule extends OxalisModule {

    private static final String USER_AGENT = String.format("Oxalis %s", OxalisVersion.getVersion());

    @Override
    protected void configure() {
        bindSettings(HttpConf.class);
    }

    @Provides
    @Singleton
    protected PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager(Settings<HttpConf> settings) {
        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager(settings.getInt(HttpConf.POOL_TIME_TO_LIVE), TimeUnit.SECONDS);
        httpClientConnectionManager.setDefaultMaxPerRoute(settings.getInt(HttpConf.POOL_MAX_ROUTE));
        httpClientConnectionManager.setMaxTotal(settings.getInt(HttpConf.POOL_TOTAL));
        httpClientConnectionManager.setValidateAfterInactivity(settings.getInt(HttpConf.POOL_VALIDATE_AFTER_INACTIVITY));

        return httpClientConnectionManager;
    }

    @Provides
    @Singleton
    protected RequestConfig getRequestConfig(Settings<HttpConf> settings) {
        return RequestConfig.custom()
                .setConnectTimeout(settings.getInt(HttpConf.TIMEOUT_CONNECT))
                .setConnectionRequestTimeout(settings.getInt(HttpConf.TIMEOUT_READ))
                .setSocketTimeout(settings.getInt(HttpConf.TIMEOUT_SOCKET))
                .build();
    }

    @Provides
    protected CloseableHttpClient getHttpClient(PoolingHttpClientConnectionManager connectionManager,
                                                RequestConfig requestConfig, Tracer tracer) {
        HttpClientBuilder httpClientBuilder = new TracingHttpClientBuilder().withTracer(tracer);

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
