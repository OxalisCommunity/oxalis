package eu.peppol.outbound.transmission;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.RawStatisticsRepositoryFactoryProvider;
import eu.peppol.util.GlobalConfiguration;

/**
 * @author steinar
 *         Date: 04.11.13
 *         Time: 10:06
 */
public class TransmissionModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    @Named("OurCommonName")
    CommonName ourCommonName() {
        return KeystoreManager.getInstance().getOurCommonName();
    }

    @Provides
    GlobalConfiguration obtainConfiguration() {
        return GlobalConfiguration.getInstance();
    }

    @Provides
    RawStatisticsRepository obtainRawStaticsRepository() {
        RawStatisticsRepository repository = RawStatisticsRepositoryFactoryProvider.getInstance().getInstanceForRawStatistics();
        return repository;
    }

}
