/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.inbound.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import eu.peppol.as2.evidence.TransportEvidenceModule;
import eu.peppol.persistence.RepositoryModule;
import eu.peppol.security.SecurityModule;

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
                new RepositoryModule(),
                new TransportEvidenceModule(),
                new SecurityModule(),
                // Provided by Guice
                new ServletModule(){

                    @Override
                    protected void configureServlets() {
                        serve("/as2*").with(AS2Servlet.class);
                    }
                }
        );
    }
}
