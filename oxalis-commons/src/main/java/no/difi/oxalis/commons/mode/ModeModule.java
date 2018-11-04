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

package no.difi.oxalis.commons.mode;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.spanmanager.DefaultSpanManager;
import lombok.extern.slf4j.Slf4j;
import net.klakegg.pkix.ocsp.api.OcspFetcher;
import no.difi.certvalidator.api.CrlCache;
import no.difi.certvalidator.api.CrlFetcher;
import no.difi.certvalidator.util.SimpleCrlCache;
import no.difi.oxalis.api.lang.OxalisLoadingException;
import no.difi.oxalis.commons.guice.OxalisModule;
import no.difi.vefa.peppol.common.lang.PeppolLoadingException;
import no.difi.vefa.peppol.mode.Mode;
import no.difi.vefa.peppol.security.ModeDetector;
import no.difi.vefa.peppol.security.api.CertificateValidator;
import org.apache.http.client.config.RequestConfig;

import javax.inject.Named;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * @author erlend
 * @since 4.0.0
 */
@Slf4j
public class ModeModule extends OxalisModule {

    @Override
    protected void configure() {
        bind(OcspFetcher.class).to(OxalisOcspFetcher.class);
        bind(CrlCache.class).toInstance(new SimpleCrlCache());
        bind(CrlFetcher.class).to(OxalisCrlFetcher.class);
    }

    @Provides
    @Singleton
    protected Mode providesMode(X509Certificate certificate, Config config, OcspFetcher ocspFetcher,
                                CrlFetcher crlFetcher, Tracer tracer)
            throws PeppolLoadingException {
        Span span = tracer.buildSpan("Mode detection").start();
        DefaultSpanManager.getInstance().activate(span);
        try {
            Map<String, Object> objectStorage = new HashMap<>();
            objectStorage.put("ocsp_fetcher", ocspFetcher);
            objectStorage.put("crlFetcher", crlFetcher);

            Mode mode = ModeDetector.detect(certificate, config, objectStorage);
            log.info("Detected mode: {}", mode.getIdentifier());
            return mode;
        } finally {
            span.finish();
        }
    }

    @Provides
    @Singleton
    protected CertificateValidator getCertificateValidator(Mode mode) throws PeppolLoadingException {
        return mode.initiate("security.validator.class", CertificateValidator.class);
    }

    @Provides
    @Singleton
    @Named("certificate")
    protected RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(10 * 1000)
                .setConnectionRequestTimeout(10 * 1000)
                .build();
    }
}
