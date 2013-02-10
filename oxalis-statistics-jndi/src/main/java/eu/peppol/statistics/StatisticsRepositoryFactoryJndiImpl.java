package eu.peppol.statistics;

import eu.peppol.util.GlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Provides StatisticsRepository objects, ready to use with a DataSource injected etc.
 * <p/>
 * This implementation will use JNDI to obtain a DataSource, which is injected into the StatisticsRepository objects to create a connection pool.
 * <p/>
 * User: steinar
 * Date: 10.02.13
 * Time: 21:53
 */
public class StatisticsRepositoryFactoryJndiImpl implements StatisticsRepositoryFactory {

    public static final Logger log = LoggerFactory.getLogger(StatisticsRepositoryFactoryJndiImpl.class);
    public static final String JAVA_COMP_ENV = "java:comp/env";

    DataSource dataSource;
    private final GlobalConfiguration globalConfiguration;

    public StatisticsRepositoryFactoryJndiImpl() {
        this.globalConfiguration = GlobalConfiguration.getInstance();
    }

    @Override
    public StatisticsRepository getInstance() {
        return new StatisticsRepositoryJdbcImpl(getDataSource());
    }

    private synchronized DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = obtainDataSourceFromJNDI();
        }

        return dataSource;
    }


    DataSource obtainDataSourceFromJNDI() {
        String dataSourceJndiName = globalConfiguration.getDataSourceJndiName();

        log.debug("Initializing the MessageDBMS persistence, obtaining data source from JNDI: " + JAVA_COMP_ENV + "/" + dataSourceJndiName);
        try {
            Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup(JAVA_COMP_ENV);
            dataSource = (DataSource) envCtx.lookup(dataSourceJndiName);
        } catch (NamingException e) {
            log.error("Unable to create initial JNDI context. " + e, e);
            log.error("You need to inspect your JNDI configuration in your web application environment, i.e. the context.xml and server.xml in Tomcat.");
        }
        return dataSource;
    }
}
