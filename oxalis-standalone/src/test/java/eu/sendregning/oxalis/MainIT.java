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

package eu.sendregning.oxalis;

import com.google.inject.Inject;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OperationalMode;
import eu.peppol.util.OxalisProductionConfigurationModule;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.testng.Assert.*;

/**
 * TODO Enable testing.
 *
 * @author steinar
 *         Date: 08.11.13
 *         Time: 14:17
 */
@Test(groups = {"integration"})
@Guice(modules = { OxalisProductionConfigurationModule.class })
public class MainIT {


    @Inject
    GlobalConfiguration globalConfiguration;


    File systemTempDir() {
        String tmpDirName = System.getProperty("java.io.tmpdir");
        assertNotNull(tmpDirName, "System tmp dir property java.io.tmpdir returned null");
        File tmpDir = new File(tmpDirName);
        if (!tmpDir.exists() || !tmpDir.isDirectory()) {
            throw new IllegalStateException(tmpDir + " does not exist or is not a directory");
        }
        return tmpDir;
    }

    @Test(enabled = true)
    public void sendToDifiTest() throws URISyntaxException {

        OperationalMode modeOfOperation = globalConfiguration.getModeOfOperation();
        assertEquals(modeOfOperation, OperationalMode.TEST, "This test may only be run in TEST mode");

        URL resource = MainIT.class.getClassLoader().getResource("BII04_T10_EHF-v2.0_invoice.xml");
        URI uri = resource.toURI();
        File testFile = new File(uri);
        assertTrue(testFile.canRead(), "Can not locate " + testFile);

        String[] args = {
                "-f", testFile.toString(),
                "-r", WellKnownParticipant.DIFI_TEST.toString(),
                "-s", WellKnownParticipant.U4_TEST.toString(),
                "-x", "1",
                "-e","/tmp" // current directory
        };

        // Executes the outbound message sender
        try {
            Main.main(args);
        } catch (Exception e) {
            fail("Failed " + e.getMessage(),e);
        }
    }

    /**
     * Verifies that if transmission fails, the Main task should "hang" indefinetely.
     *
     * @throws URISyntaxException
     */
    @Test(enabled = true)
    public void makeEverythingHang() throws URISyntaxException {

        OperationalMode modeOfOperation = globalConfiguration.getModeOfOperation();
        assertEquals(modeOfOperation, OperationalMode.TEST, "This test may only be run in TEST mode");

        URL resource = MainIT.class.getClassLoader().getResource("BII04_T10_EHF-v2.0_invoice.xml");
        URI uri = resource.toURI();
        File testFile = new File(uri);
        assertTrue(testFile.canRead(), "Can not locate " + testFile);

        String[] args = {
                "-f", testFile.toString(),
                "-r", WellKnownParticipant.DIFI_TEST.toString(),
                "-s", WellKnownParticipant.U4_TEST.toString(),
                "-x", "2",
                "--repeat","10",
                "-m", "as2",
                "-i", "AP_DUMYY0001",
                "-u", "http://rubbish",
                "-e","/tmp" // current directory
        };

        // Executes the outbound message sender
        try {
            Main.main(args);
        } catch (Exception e) {
            fail("Failed " + e.getMessage(),e);
        }
    }



    @Test(enabled = true)
    public void sendToHafslundTellier() throws Exception {

        OperationalMode modeOfOperation = globalConfiguration.getModeOfOperation();
        assertEquals(modeOfOperation, OperationalMode.TEST, "This test may only be run in TEST mode");

        File testFile = new File("/tmp/Out");
        assertTrue(testFile.canRead(), "Can not locate " + testFile);

        String[] args = {
                "-f", testFile.toString(),
                "-r", "9908:971589671",
                "-s", WellKnownParticipant.DUMMY.toString(),
                // "-u", "https://ap-test.hafslundtellier.no/oxalis/as2",
                "-u", "http://localhost:8080/oxalis/as2",
                "-m", "as2",
                "-i", "APP_10000XXX",
                "-t","true",
                "-e","/tmp/evidence" // current directory
        };

        // Executes the outbound message sender
        try {
            Main.main(args);
        } catch (Exception e) {
            fail("Failed " + e.getMessage(),e);
        }


    }

    @Test(enabled = false)
    public void sendToEspapTest() throws URISyntaxException {

        OperationalMode modeOfOperation = globalConfiguration.getModeOfOperation();
        assertEquals(modeOfOperation, OperationalMode.TEST, "This test may only be run in TEST mode");

        URL resource = MainIT.class.getClassLoader().getResource("BII04_T10_EHF-v2.0_invoice.xml");
        URI uri = resource.toURI();
        File testFile = new File(uri);
        assertTrue(testFile.canRead(), "Can not locate " + testFile);

        String[] args = {
                "-f", testFile.toString(),
                "-r", "9946:ESPAP",
                "-s", WellKnownParticipant.DIFI_TEST.toString(),
                "-t","true",
                "-u", "https://ap1.espap.pt/oxalis/as2",
                "-m","AS2",
                "-i","APP_1000000222"
        };

        // Executes the outbound message sender
        try {
            Main.main(args);
        } catch (Exception e) {
            fail("Failed " + e.getMessage());
        }
    }



    @Test
    public void testGetOptionParser() throws Exception {
        OptionParser optionParser = Main.getOptionParser();

        String tmpDir = systemTempDir().toString();

        OptionSet optionSet = optionParser.parse("-f","/tmp/dummy","-s", "9908:976098897","-r", "9908:810017902","-u", "https://ap.unit4.com", "-m", "as2", "-e", tmpDir);
        assertTrue(optionSet.has("u"));
        assertTrue(optionSet.has("f"));
        assertTrue(optionSet.has("e"));
        Object e = optionSet.valueOf("e");
        assertNotNull(e);
        assertTrue(e instanceof File);
        File f = (File)e;
        assertEquals(f, new File(tmpDir));
    }

    @Test
    public void senderAndReceiverIsOptional() throws Exception {
        OptionParser optionParser = Main.getOptionParser();

        OptionSet optionSet = optionParser.parse("-f","/tmp/dummy","-u", "https://ap.unit4.com", "-m", "as2");
        assertFalse(optionSet.has("-r"));
        assertFalse(optionSet.has("-s"));
    }

}
