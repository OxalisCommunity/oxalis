package eu.peppol.jdbc;

import eu.peppol.util.GlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Provides an instance of {@link javax.sql.DataSource} using the condfiguration parameters found
 * in {@link GlobalConfiguration#OXALIS_GLOBAL_PROPERTIES}, which is located in
 * OXALIS_HOME.
 *
 * @author steinar
 *         Date: 18.04.13
 *         Time: 13:28
 */
public class OxalisDataSourceFactoryJndiImpl implements OxalisDataSourceFactory {

    public static final Logger log = LoggerFactory.getLogger(OxalisDataSourceFactoryJndiImpl.class);

    @Override
    public DataSource getDataSource() {
        String dataSourceJndiName = GlobalConfiguration.getInstance().getDataSourceJndiName();

        log.debug("Obtaining data source from JNDI: " + dataSourceJndiName);
        try {
            Context initCtx = new InitialContext();
            return (DataSource) initCtx.lookup(dataSourceJndiName);
        } catch (NamingException e) {
            throw new IllegalStateException("Unable to obtain JNDI datasource from " + dataSourceJndiName + "; "+ e, e);
        }
    }
}
