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
import net.klakegg.pkix.ocsp.api.OcspFetcher;
import no.difi.certvalidator.api.CrlCache;
import no.difi.certvalidator.api.CrlFetcher;
import no.difi.certvalidator.util.SimpleCrlCache;
import no.difi.oxalis.commons.guice.OxalisModule;
import no.difi.vefa.peppol.common.lang.PeppolLoadingException;
import no.difi.vefa.peppol.mode.Mode;
import no.difi.vefa.peppol.security.ModeDetector;
import no.difi.vefa.peppol.security.api.CertificateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * @author erlend
 * @since 4.0.0
 */
public class ModeModule extends OxalisModule {

    private static Logger logger = LoggerFactory.getLogger(ModeModule.class);

    @Override
    protected void configure() {
        bind(OcspFetcher.class).to(OxalisOcspFetcher.class);
        bind(CrlCache.class).toInstance(new SimpleCrlCache());
        bind(CrlFetcher.class).to(OxalisCrlFetcher.class);
    }

    @Provides
    @Singleton
    protected Mode providesMode(X509Certificate certificate, Config config, OcspFetcher ocspFetcher, CrlFetcher crlFetcher)
            throws PeppolLoadingException {
        Map<String, Object> objectStorage = new HashMap<>();
        objectStorage.put("ocsp_fetcher", ocspFetcher);
        objectStorage.put("crlFetcher", crlFetcher);

        Mode mode = ModeDetector.detect(certificate, config, objectStorage);
        logger.info("Detected mode: {}", mode.getIdentifier());
        return mode;
    }

    @Provides
    @Singleton
    protected CertificateValidator getCertificateValidator(Mode mode) throws PeppolLoadingException {
        return mode.initiate("security.validator.class", CertificateValidator.class);
    }
}
