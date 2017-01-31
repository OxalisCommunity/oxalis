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

package eu.peppol.as2.inbound;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import eu.peppol.as2.util.MdnMimeMessageFactory;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

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
    protected MdnMimeMessageFactory provideMdnMimeMessageFactory(X509Certificate x509Certificate, PrivateKey privateKey) {
        return new MdnMimeMessageFactory(x509Certificate, privateKey);
    }
}
