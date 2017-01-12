package eu.peppol.as2.module;

import com.google.inject.servlet.ServletModule;
import eu.peppol.as2.inbound.As2Servlet;

public class As2InboundModule extends ServletModule {
    
    @Override
    protected void configureServlets() {
        serve("/as2*").with(As2Servlet.class);
    }
}
