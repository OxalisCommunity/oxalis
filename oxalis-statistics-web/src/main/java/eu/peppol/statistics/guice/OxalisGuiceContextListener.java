package eu.peppol.statistics.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.JerseyServletModule;

/**
 * @author steinar
 *         Date: 15.04.13
 *         Time: 21:04
 */
public class OxalisGuiceContextListener extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new OxalisRestModule());
    }
}
