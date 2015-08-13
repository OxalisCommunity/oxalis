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

package eu.peppol.inbound.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WSWebServiceContext;
import com.sun.xml.ws.server.AbstractMultiInstanceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.WebServiceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * @author steinar
 *         Date: 09.06.13
 *         Time: 15:38
 */

public class GuiceManagedInstanceResolver<T> extends AbstractMultiInstanceResolver<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuiceManagedInstanceResolver.class);

    private static Injector injector;

    private transient WSWebServiceContext webServiceContext;

    public GuiceManagedInstanceResolver(final Class<T> clazz) {
        super(clazz);
    }

    @Override
    public T resolve(final Packet request) {
        final T instance = injector.getInstance(this.clazz);
        injector.injectMembers(instance);

        return instance;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void start(final WSWebServiceContext wsWebServiceContext, final WSEndpoint endpoint) {
        super.start(wsWebServiceContext, endpoint);
        this.webServiceContext = wsWebServiceContext;

        synchronized (GuiceManagedInstanceResolver.class) {
            if (injector == null) {
                final List<Module> moduleInstances = new ArrayList<Module>();
                final Class<? extends Module>[] modules = this.clazz.getAnnotation(GuiceManaged.class).modules();

                for (final Class<? extends Module> moduleClass : modules) {
                    try {
                        moduleInstances.add(moduleClass.newInstance());
                    } catch (final InstantiationException exception) {
                        LOGGER.error("Could not instantiate guice module [{}]", moduleClass.getName());
                    } catch (final IllegalAccessException e) {
                        LOGGER.error("Could not instantiate guice module [{}]", moduleClass.getName());
                    }
                }

                moduleInstances.add(new AbstractModule() {
                    @Override
                    protected void configure() {
                        this.bind(WebServiceContext.class).toInstance(GuiceManagedInstanceResolver.this.webServiceContext);
                    }
                });

                injector = Guice.createInjector(moduleInstances);
            }
        }
    }
}
