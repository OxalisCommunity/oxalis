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

package eu.peppol.util;

import com.google.inject.Inject;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.File;
import java.io.PrintStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * User: steinar
 * Date: 04.10.12
 * Time: 13:42
 */
@Test(groups = "integration")
@Guice(modules = {OxalisProductionConfigurationModule.class})
public class LoggingConfiguratorIT {

    public static final String FILE_NAME = "logback-test.xml";
    private PrintStream out;

    @Inject
    GlobalConfiguration globalConfiguration;

    @BeforeMethod
    public void redirectStdoutAndStderr() {
        out = System.out;
    }

    @AfterTest
    public void restoreOutputStreams(){
        System.setOut(out);
    }

    @Test
    public void locateConfigurationFileInClassPath() {
        LoggingConfigurator loggingConfigurator = new LoggingConfigurator(globalConfiguration);

        File logConfigFile = loggingConfigurator.locateLoggingConfigurationFileInClassPathBySimpleName(FILE_NAME);
        assertNotNull(logConfigFile,FILE_NAME + " not located in class path,");
        assertEquals(logConfigFile.getName(), FILE_NAME);
    }

    @Test
    public void comfigureLoggingUsingDefaultConfigFile() {
        LoggingConfigurator lc = new LoggingConfigurator(globalConfiguration);
        lc.execute();
    }
}
