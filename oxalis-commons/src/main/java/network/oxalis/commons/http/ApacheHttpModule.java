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

package network.oxalis.commons.http;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.apachehttpclient.v5_2.ApacheHttpClientTelemetry;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.guice.OxalisModule;
import network.oxalis.commons.util.OxalisVersion;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import java.time.Duration;

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
        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager();

        connectionManager.setDefaultMaxPerRoute(settings.getInt(HttpConf.POOL_MAX_ROUTE));
        connectionManager.setMaxTotal(settings.getInt(HttpConf.POOL_TOTAL));
        connectionManager.setValidateAfterInactivity(TimeValue.ofSeconds(settings.getInt(HttpConf.POOL_VALIDATE_AFTER_INACTIVITY)));

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setTimeToLive(TimeValue.of(Duration.ofSeconds(settings.getInt(HttpConf.POOL_TIME_TO_LIVE))))
                .build();
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        return connectionManager;
    }

    @Provides
    @Singleton
    protected RequestConfig getRequestConfig(Settings<HttpConf> settings) {
        return RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(settings.getInt(HttpConf.TIMEOUT_CONNECT)))
                .setResponseTimeout(Timeout.ofMilliseconds(settings.getInt(HttpConf.TIMEOUT_READ)))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(settings.getInt(HttpConf.TIMEOUT_SOCKET)))
                .build();
    }

    @Provides
    protected CloseableHttpClient getHttpClient(PoolingHttpClientConnectionManager connectionManager,
                                                RequestConfig requestConfig, OpenTelemetry openTelemetry) {
        ApacheHttpClientTelemetry telemetry = ApacheHttpClientTelemetry.builder(openTelemetry).build();

        return HttpClients.custom()
                .setUserAgent(USER_AGENT)
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .setConnectionManagerShared(true)
                .useSystemProperties()
                .build();
    }
}
