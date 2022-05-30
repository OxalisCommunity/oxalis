/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package network.oxalis.persistence.platform;

import network.oxalis.api.lang.OxalisLoadingException;
import network.oxalis.persistence.api.JdbcTxManager;
import network.oxalis.persistence.api.Platform;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;

/**
 * @author erlend
 */
public class PlatformProviderTest {

    private JdbcTxManager jdbcTxManager;

    private Connection connection;

    private DatabaseMetaData databaseMetaData;

    @BeforeClass
    public void beforeMethod() {
        jdbcTxManager = Mockito.mock(JdbcTxManager.class);
        connection = Mockito.mock(Connection.class);
        databaseMetaData = Mockito.mock(DatabaseMetaData.class);
    }

    @Test
    public void simple() throws Exception {
        Mockito.doReturn(connection).when(jdbcTxManager).getConnection();
        Mockito.doReturn(databaseMetaData).when(connection).getMetaData();
        Mockito.doReturn("H2").when(databaseMetaData).getDatabaseProductName();

        Platform platform = new H2Platform();

        PlatformProvider platformProvider = new PlatformProvider(jdbcTxManager, Collections.singleton(platform));

        Assert.assertEquals(platformProvider.get(), platform);
    }

    @Test(expectedExceptions = OxalisLoadingException.class)
    public void notFound() throws Exception {
        Mockito.doReturn(connection).when(jdbcTxManager).getConnection();
        Mockito.doReturn(databaseMetaData).when(connection).getMetaData();
        Mockito.doReturn("MySQL").when(databaseMetaData).getDatabaseProductName();

        Platform platform = new H2Platform();

        PlatformProvider platformProvider = new PlatformProvider(jdbcTxManager, Collections.singleton(platform));

        platformProvider.get();
    }

    @Test(expectedExceptions = OxalisLoadingException.class)
    public void exception() throws Exception {
        Mockito.doReturn(connection).when(jdbcTxManager).getConnection();
        Mockito.doThrow(new SQLException()).when(connection).getMetaData();

        Platform platform = new H2Platform();

        PlatformProvider platformProvider = new PlatformProvider(jdbcTxManager, Collections.singleton(platform));

        platformProvider.get();
    }
}
