/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.peppol.persistence.jdbc.platform;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author steinar
 *         Date: 06.11.2016
 *         Time: 17.24
 */
public class DbmsPlatformFactory {

    public static DbmsPlatform platformFor(Connection connection) {
        try {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            if (databaseProductName.toLowerCase().contains("microsoft")) {
                return new MsSqlServerPlatform(databaseProductName);
            } else if (databaseProductName.toLowerCase().contains("mysql")) {
                return new MySqlPlatform(databaseProductName);
            } else if (databaseProductName.toLowerCase().contains("h2")) {
                return new H2DatabasePlatform(databaseProductName);
            } else
                return new GenericDbmsPlatform(databaseProductName);

        } catch (SQLException e) {
            throw new IllegalStateException("Unable to obtain database product name. " + e.getMessage(), e);
        }
    }
}
