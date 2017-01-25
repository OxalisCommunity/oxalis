package eu.peppol.as2.inbound;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import eu.peppol.as2.util.MdnMimeMessageFactory;
import eu.peppol.security.KeystoreManager;

/**
 * Guice module providing AS2 implementation for inbound.
 *
 * @author erlend
 * @since 4.0.0
 */
public class As2InboundModule extends ServletModule {

    @Override
    protected void configureServlets() {
        serve("/as2*").with(As2Servlet.class);
    }

    @Provides
    @Singleton
    protected MdnMimeMessageFactory provideMdnMimeMessageFactory(KeystoreManager keystoreManager) {
        return new MdnMimeMessageFactory(keystoreManager.getOurCertificate(), keystoreManager.getOurPrivateKey());
    }
}
