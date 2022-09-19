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

package network.oxalis.commons.mode;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.typesafe.config.Config;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.spanmanager.DefaultSpanManager;
import network.oxalis.pkix.ocsp.api.OcspFetcher;
import network.oxalis.commons.certvalidator.api.CrlFetcher;
import network.oxalis.api.lang.OxalisLoadingException;
import network.oxalis.vefa.peppol.common.lang.PeppolLoadingException;
import network.oxalis.vefa.peppol.mode.Mode;
import network.oxalis.vefa.peppol.security.ModeDetector;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * @author erlend
 * @since 4.0.4
 *
 * @author aaron-kumar
 * @since 5.0.0
 */
public class ModeProvider implements Provider<Mode> {

    @Inject
    private X509Certificate certificate;

    @Inject
    private Config config;

    @Inject
    private OcspFetcher ocspFetcher;

    @Inject
    private CrlFetcher crlFetcher;

    @Inject
    private Tracer tracer;

    @Override
    public Mode get() {
        Span span = tracer.buildSpan("Mode detection").start();
        DefaultSpanManager.getInstance().activate(span);
        try {
            Map<String, Object> objectStorage = new HashMap<>();
            objectStorage.put("ocsp_fetcher", ocspFetcher);
            objectStorage.put("crlFetcher", crlFetcher);

            return ModeDetector.detect(certificate, config, objectStorage);
        } catch (PeppolLoadingException e) {
            throw new OxalisLoadingException("Unable to detect mode.", e);
        } finally {
            span.finish();
        }
    }
}
