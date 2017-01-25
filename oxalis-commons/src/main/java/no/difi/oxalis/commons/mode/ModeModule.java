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

package no.difi.oxalis.commons.mode;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import no.difi.vefa.peppol.common.lang.PeppolLoadingException;
import no.difi.vefa.peppol.mode.Mode;
import no.difi.vefa.peppol.security.ModeDetector;
import no.difi.vefa.peppol.security.api.CertificateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.security.cert.X509Certificate;

/**
 * @author erlend
 * @since 4.0.0
 */
public class ModeModule extends AbstractModule {

    private static Logger logger = LoggerFactory.getLogger(ModeModule.class);

    @Override
    protected void configure() {
        // No action.
    }

    @Provides
    @Singleton
    protected Mode providesMode(X509Certificate certificate) throws PeppolLoadingException {
        Mode mode = ModeDetector.detect(certificate);
        logger.info("Detected mode: {}", mode.getIdentifier());
        return mode;
    }

    @Provides
    @Singleton
    protected CertificateValidator getCertificateValidator(Mode mode) throws PeppolLoadingException {
        return mode.initiate("security.validator.class", CertificateValidator.class);
    }

    @Provides
    @Singleton
    protected Config getConfig(Mode mode) {
        return mode.getConfig();
    }
}
