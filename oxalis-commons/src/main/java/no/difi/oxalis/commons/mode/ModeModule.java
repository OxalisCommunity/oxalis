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
