package no.difi.oxalis.commons.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import no.difi.vefa.peppol.common.lang.PeppolLoadingException;
import no.difi.vefa.peppol.mode.Mode;
import no.difi.vefa.peppol.security.ModeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;

public class ModeModule extends AbstractModule {

    private static Logger logger = LoggerFactory.getLogger(ModeModule.class);

    @Override
    protected void configure() {

    }

    @Provides
    Mode providesMode(X509Certificate certificate) throws PeppolLoadingException{
        Mode mode = ModeDetector.detect(certificate);
        logger.info("Detected mode: {}", mode.getIdentifier());
        return mode;
    }
}
