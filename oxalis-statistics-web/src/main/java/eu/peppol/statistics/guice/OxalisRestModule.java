package eu.peppol.statistics.guice;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import eu.peppol.statistics.resource.HelloWorldResource;
import eu.peppol.statistics.resource.MessageFactResource;
import sun.plugin2.message.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * @author steinar
 *         Date: 15.04.13
 *         Time: 21:17
 */
public class OxalisRestModule extends JerseyServletModule {

    private final Map<String, String> initalisationParams;

    public OxalisRestModule() {
        initalisationParams = new HashMap<String, String>();
    }

    @Override
    public void configureServlets() {

        configureLogging();
        bind(HelloWorldResource.class);
        bind(MessageFactResource.class);

        serve("/resource/*").with(GuiceContainer.class, initalisationParams);
    }

    private void configureLogging() {
        //sets up logging of requests and responses
//        if (enableTracingDebug) {
            initalisationParams.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, "com.sun.jersey.api.container.filter.LoggingFilter");
            initalisationParams.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, "com.sun.jersey.api.container.filter.LoggingFilter");
            initalisationParams.put(ResourceConfig.FEATURE_TRACE, "true");
//        }
    }
}
