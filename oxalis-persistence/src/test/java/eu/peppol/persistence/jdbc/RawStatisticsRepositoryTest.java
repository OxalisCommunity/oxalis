package eu.peppol.persistence.jdbc;

import eu.peppol.persistence.guice.TestModuleFactory;
import eu.peppol.statistics.RawStatisticsRepository;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 28.10.2016
 *         Time: 17.23
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class RawStatisticsRepositoryTest {

    @Inject
    RawStatisticsRepository rawStatisticsRepository;

    @Test
    public void testInjection(){

        assertNotNull(rawStatisticsRepository);
    }

}
