package eu.peppol.inbound;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import eu.peppol.inbound.server.OxalisGuiceContextListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public abstract class AbstractJettyServerTest {

    private Injector injector;

    protected Server server;

    public Injector getInjector() {
        return new OxalisGuiceContextListener().getInjector();
    }

    @BeforeClass
    public void beforeClass() throws Exception {
        injector = getInjector();

        server = new Server(8080);

        ServletContextHandler handler = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        handler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(DefaultServlet.class, "/");

        server.start();
    }

    @AfterClass
    public void afterClass() throws Exception {
        server.stop();
    }
}
