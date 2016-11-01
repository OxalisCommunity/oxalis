package eu.peppol.persistence.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.RepositoryConfiguration;
import eu.peppol.persistence.api.account.AccountRepository;
import eu.peppol.persistence.file.ArtifactPathComputer;
import eu.peppol.persistence.jdbc.*;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.RawStatisticsRepositoryFactory;

import javax.inject.Named;
import java.nio.file.Path;

/**
 * Wires up the persistence component.
 * <p>
 * NOTE! When creating an injector, remember to supply an instance of {@link javax.sql.DataSource}
 *
 * @author steinar
 *         Date: 25.10.2016
 *         Time: 21.43
 */
public class RepositoryModule extends AbstractModule {

    @Override
    protected void configure() {


        Binder binder = binder();

        // Includes the Aop based Tx manager, which needs a DataSource
        binder.install(new AopJdbcTxManagerModule());

        // The repositories
        bind(AccountRepository.class).to(AccountRepositoryImpl.class);
        bind(MessageRepository.class).to(MessageRepositoryH2Impl.class);

        rawStatisticsBindings();

        bind(ArtifactPathComputer.class);
    }

    private void rawStatisticsBindings() {
        bind(RawStatisticsRepository.class).annotatedWith(Names.named("H2")).to(RawStatisticsRepositoryMsSqlImpl.class);
        bind(RawStatisticsRepository.class).annotatedWith(Names.named("MySQL")).to(RawStatisticsRepositoryMySqlImpl.class);
        bind(RawStatisticsRepository.class).annotatedWith(Names.named("MsSql")).to(RawStatisticsRepositoryMsSqlImpl.class);
        bind(RawStatisticsRepository.class).annotatedWith(Names.named("Oracle")).to(RawStatisticsRepositoryOracleImpl.class);
        bind(RawStatisticsRepository.class).annotatedWith(Names.named("HSqlDB")).to(RawStatisticsRepositoryHSqlImpl.class);

        bind(RawStatisticsRepositoryFactory.class).to(RawStatisticsRepositoryFactoryJdbcImpl.class).in(Singleton.class);
    }

    @Provides
    RawStatisticsRepository rawStatisticsRepository(RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory) {
        return rawStatisticsRepositoryFactory.getInstanceForRawStatistics();
    }


    /**
     * The {@link RepositoryConfiguration} instance must be supplied by another Google Guice module during creation of the Injector.
     *
     * @param repositoryConfiguration an instance supplied by another module in the same Google Guice injector.
     * @return repository configuration
     */
    @Provides
    @Named(RepositoryConfiguration.BASE_PATH_NAME)
    public Path getBasePath(RepositoryConfiguration repositoryConfiguration) {
        return repositoryConfiguration.getBasePath();
    }

}
