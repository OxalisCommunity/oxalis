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

import network.oxalis.persistence.api.Platform;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author erlend
 */
public class PlatformsTest {

    @Test
    public void h2() {
        Platform platform = new H2Platform();

        Assert.assertTrue(platform.detect("H2"));
        Assert.assertFalse(platform.detect("MySQL"));

        Assert.assertEquals(platform.getIdentifier(), H2Platform.IDENTIFIER);
        Assert.assertEquals(platform.getNamed().value(), H2Platform.IDENTIFIER);
    }

    @Test
    public void hsqldb() {
        Platform platform = new HSQLDBPlatform();

        Assert.assertTrue(platform.detect("HSqlDB"));
        Assert.assertFalse(platform.detect("MySQL"));

        Assert.assertEquals(platform.getIdentifier(), HSQLDBPlatform.IDENTIFIER);
    }

    @Test
    public void mssql() {
        Platform platform = new MsSQLPlatform();

        Assert.assertFalse(platform.detect("H2"));
        Assert.assertTrue(platform.detect("Microsoft"));

        Assert.assertEquals(platform.getIdentifier(), MsSQLPlatform.IDENTIFIER);
    }

    @Test
    public void mysql() {
        Platform platform = new MySQLPlatform();

        Assert.assertFalse(platform.detect("H2"));
        Assert.assertTrue(platform.detect("MySQL"));

        Assert.assertEquals(platform.getIdentifier(), MySQLPlatform.IDENTIFIER);
    }

    @Test
    public void oracle() {
        Platform platform = new OraclePlatform();

        Assert.assertFalse(platform.detect("H2"));
        Assert.assertTrue(platform.detect("Oracle"));

        Assert.assertEquals(platform.getIdentifier(), OraclePlatform.IDENTIFIER);
    }
}
