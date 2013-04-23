package eu.peppol.statistics;

import java.sql.ResultSet;

/**
 * @author steinar
 *         Date: 23.04.13
 *         Time: 23:53
 */
public interface ResultSetWriter {

    void writeAll(ResultSet resultSet);
}
