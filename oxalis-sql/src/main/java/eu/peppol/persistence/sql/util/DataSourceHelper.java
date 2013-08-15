package eu.peppol.persistence.sql.util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author steinar
 *         Date: 15.08.13
 *         Time: 15:50
 */
public class DataSourceHelper {

    private final DataSource dataSource;

    public DataSourceHelper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnectionWithAutoCommit() {
        return getConnection(true);
    }

    public  Connection getConnectionNoAutoCommit() {
        Connection con = getConnection(false);
        return con;
    }

    public Connection getConnection(boolean autoCommit) {
        Connection con;
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to retrieve connection " + e, e);
        }

        return con;
    }

    public static void close(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                throw new IllegalStateException("Unable to close JDBC connection " + con);
            }
        }
    }

}
