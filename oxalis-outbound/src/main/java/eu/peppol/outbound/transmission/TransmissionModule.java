package eu.peppol.outbound.transmission;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.smp.SmpLookupManagerImpl;
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

        // TODO: refactor the binding of SmpLookupManager into separate module by moving the inclusion of SmpModule into eu.peppol.outbound.OxalisOutboundModule
        bind(SmpLookupManager.class).to(SmpLookupManagerImpl.class);

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
