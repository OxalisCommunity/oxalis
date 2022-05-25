package network.oxalis.test.asd;

import com.google.inject.servlet.ServletModule;

/**
 * @author erlend
 */
public class AsdInboundModule extends ServletModule {

    @Override
    protected void configureServlets() {
        serve("/asd").with(AsdServlet.class);
    }
}
