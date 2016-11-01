package eu.peppol.persistence.jdbc.util;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.SQLException;

/**
 * @author steinar
 *         Date: 22.10.2016
 *         Time: 12.35
 */
public class InMemoryDatabaseHelper {

    public static final Logger log = LoggerFactory.getLogger(InMemoryDatabaseHelper.class);

    public static final String CREATE_OXALIS_DBMS_H2_SQL = "sql/create-oxalis-dbms-h2.sql";

    public static DataSource createInMemoryDatabase() {

        // Creates the empty database
        DataSource ds = createEmptyInMemoryDatabase();

        // Loads the SQL schema into the database, i.e creates tables etc.
        createDatabaseSchema(ds);

        return ds;
    }

    private static void createDatabaseSchema(DataSource ds) {
        try (InputStream resourceAsStream = InMemoryDatabaseHelper.class.getClassLoader().getResourceAsStream(CREATE_OXALIS_DBMS_H2_SQL);){
            log.info("Creating the Oxalis database");

            RunScript.execute(ds.getConnection(), new InputStreamReader(resourceAsStream, Charset.forName("UTF-8")));
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to obtain connection from datasource. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load SQL script from " + CREATE_OXALIS_DBMS_H2_SQL + " : " + e.getMessage(), e);
        }
    }

    public static DataSource createEmptyInMemoryDatabase() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        //ds.setUrl("jdbc:h2:~/test;AUTO_SERVER=TRUE");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }
}
