package eu.peppol.jdbc;

import eu.peppol.util.GlobalConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Provides an instance of {@link javax.sql.DataSource} using the condfiguration parameters found
 * in {@link GlobalConfigurationImpl#OXALIS_GLOBAL_PROPERTIES}, which is located in
 * OXALIS_HOME.
 *
 * @author steinar
 *         Date: 18.04.13
 *         Time: 13:28
 */
public class OxalisDataSourceFactoryJndiImpl implements OxalisDataSourceFactory {


    public static final Logger log = LoggerFactory.getLogger(OxalisDataSourceFactoryJndiImpl.class);

    public static final String JAVA_COMP_ENV = "java:comp/env";


    @Override
    public DataSource getDataSource() {
        String dataSourceJndiName = new GlobalConfigurationImpl().getDataSourceJndiName();

        log.debug("Obtaining data source from JNDI: " + JAVA_COMP_ENV + "/" + dataSourceJndiName);
        try {
            Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup(JAVA_COMP_ENV);
            return (DataSource) envCtx.lookup(dataSourceJndiName);
        } catch (NamingException e) {
            throw new IllegalStateException("Unable to obtain JNDI datasource from " + JAVA_COMP_ENV + "/" + dataSourceJndiName + "; "+ e, e);
        }
    }
}
