package eu.peppol.persistence.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import eu.peppol.jdbc.OxalisDataSourceFactory;
import eu.peppol.persistence.jdbc.OxalisDataSourceFactoryDbcpImpl;

import javax.sql.DataSource;

/**
 * Guice module providing a {@link DataSource} configured and bound to an SQL DBMS
 * based upon the contents of a configuration file.
 *
 * @author steinar
 *         Date: 28.10.2016
 *         Time: 09.07
 */
public class OxalisDataSourceModule extends AbstractModule {


    @Override
    protected void configure() {
        bind(OxalisDataSourceFactory.class).to(OxalisDataSourceFactoryDbcpImpl.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    DataSource dataSourceProvider(OxalisDataSourceFactory oxalisDataSourceFactory){
        return oxalisDataSourceFactory.getDataSource();
    }
}
