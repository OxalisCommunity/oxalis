package eu.peppol.persistence.guice;

import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.testng.annotations.Test;

import static org.testng.Assert.fail;

/**
 * @author steinar
 *         Date: 28.10.2016
 *         Time: 08.38
 */
public class RepositoryModuleTest {

    @Test
    public void testConfigure() throws Exception {

        try {
            Injector injector = Guice.createInjector(new RepositoryModule());
            fail("Should not be able to create injector with supplying a module providing \n" +
                    "an instance of  DataSource and RepositoryConfiguration");
        } catch (CreationException e) {
            // As expected
        }
    }

}