/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

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
