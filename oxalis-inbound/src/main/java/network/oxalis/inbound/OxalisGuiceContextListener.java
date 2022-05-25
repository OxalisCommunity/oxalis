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

package network.oxalis.inbound;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.commons.guice.GuiceModuleLoader;

/**
 * Wires our object graph together using Google Guice.
 *
 * @author steinar
 *         Date: 29.11.13
 *         Time: 10:26
 * @author erlend
 */
@Slf4j
public class OxalisGuiceContextListener extends GuiceServletContextListener {

    private Injector injector;

    public OxalisGuiceContextListener() {
        try {
            this.injector = GuiceModuleLoader.initiate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public OxalisGuiceContextListener(Injector injector) {
        this.injector = injector;
    }

    @Override
    public Injector getInjector() {
        return injector;
    }
}
