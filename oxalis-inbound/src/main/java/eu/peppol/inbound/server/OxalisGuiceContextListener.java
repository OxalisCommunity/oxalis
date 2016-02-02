/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.inbound.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import eu.peppol.as2.As2Module;
import eu.peppol.util.OxalisCommonsModule;

/**
 * Wires our object graph together using Google Guice.
 *
 * @author steinar
 *         Date: 29.11.13
 *         Time: 10:26
 */
public class OxalisGuiceContextListener extends GuiceServletContextListener {


    @Override
    protected Injector getInjector() {

        return Guice.createInjector(
                new OxalisCommonsModule(),

                new As2Module(),
                // SevletModule is provided by Guice
                new ServletModule(){

                    @Override
                    protected void configureServlets() {
                        serve("/as2*").with(AS2Servlet.class);
                        serve("/status").with(StatusServlet.class);
                        serve("/statistics/*").with(StatisticsServlet.class);
                    }
                }
        );
    }
}
