package eu.peppol.start.persistence;

import eu.peppol.jdbc.OxalisDataSourceFactory;

import javax.sql.DataSource;

/**
 * A dummy OxalisDataSourceFactory, which is used for testing purposes.
 *
 * @author steinar
 *         Date: 02.12.13
 *         Time: 19:30
 */
public class DummyOxalisDataSourceFactory implements OxalisDataSourceFactory {
    @Override
    public DataSource getDataSource() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
