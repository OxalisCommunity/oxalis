package eu.peppol.inbound.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import eu.peppol.inbound.soap.PeppolMessageHeaderParser;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 24.10.13
 *         Time: 09:35
 */
public class VerifyAutomaticGuiceInjectionTest {


    @Test
    public void testInject() throws Exception {

        Injector injector = Guice.createInjector(new SampleModule());
        Sample instance = injector.getInstance(Sample.class);
        assertNotNull(instance.getPeppolMessageHeaderParser());

    }

    /** Sample class into which we want stuff injected */
    public static class Sample {

        // THIS INSTANCE SHOULD BE INJECTED AUTOMAGICALLY
        @Inject
        PeppolMessageHeaderParser peppolMessageHeaderParser;

        public PeppolMessageHeaderParser getPeppolMessageHeaderParser() {
            return peppolMessageHeaderParser;
        }
    }

    /** Simple Guice module */
    public static class SampleModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(Sample.class);
        }
    }
}
