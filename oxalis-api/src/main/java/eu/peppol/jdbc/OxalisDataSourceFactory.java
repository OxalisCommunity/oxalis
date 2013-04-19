package eu.peppol.jdbc;

import javax.sql.DataSource;

/**
 * @author steinar
 *         Date: 18.04.13
 *         Time: 13:43
 */
public interface OxalisDataSourceFactory {
    DataSource getDataSource();
}
