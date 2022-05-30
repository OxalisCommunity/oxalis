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

package network.oxalis.as2.inbound;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import javax.servlet.http.HttpServlet;

/**
 * Guice module providing AS2 implementation for inbound.
 *
 * @author erlend
 * @since 4.0.0
 */
public class As2InboundModule extends ServletModule {

    @Override
    protected void configureServlets() {
        bind(Key.get(HttpServlet.class, Names.named("oxalis-as2")))
                .to(As2Servlet.class)
                .asEagerSingleton();

        serve("/as2*").with(Key.get(HttpServlet.class, Names.named("oxalis-as2")));
    }
}
