package eu.peppol.statistics.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import eu.peppol.statistics.StatisticsRepository;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 18.04.13
 *         Time: 11:25
 */
@Test(groups = "integration")
@Guice( modules=RepositoryModule.class)
public class RepositoryModuleTest {

    @Test(groups = {"integration"})
    public void testRespositoryShouldBeSingleton() {


        Injector injector = com.google.inject.Guice.createInjector(new RepositoryModule());
        Dummy dummy = injector.getInstance(Dummy.class);
        Dummy dummy2 = injector.getInstance(Dummy.class);

        assertEquals(dummy.getRepository(), dummy2.getRepository());

    }

    public static class Dummy {

        private final StatisticsRepository repository;

        @Inject
        public Dummy(StatisticsRepository repository) {

            this.repository = repository;
        }

        public StatisticsRepository getRepository() {
            return repository;
        }
    }
}
