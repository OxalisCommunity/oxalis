package eu.sendregning.oxalis;

import com.google.inject.Inject;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OperationalMode;
import eu.peppol.util.RuntimeConfigurationModule;
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
 * @author steinar
 *         Date: 08.11.13
 *         Time: 14:17
 */
@Guice(modules = {RuntimeConfigurationModule.class})
public class MainTest {

    @Inject
    GlobalConfiguration globalConfiguration;

    @Test(enabled = true)
    public void sendToDifiTest() throws URISyntaxException {

        OperationalMode modeOfOperation = globalConfiguration.getModeOfOperation();
        assertEquals(modeOfOperation, OperationalMode.TEST, "This test may only be run in TEST mode");

        URL resource = MainTest.class.getClassLoader().getResource("BII04_T10_EHF-v1.5_invoice.xml");
        URI uri = resource.toURI();
        File testFile = new File(uri);
        assertTrue(testFile.canRead(), "Can not locate " + testFile);

        String[] args = {
                "-f", testFile.toString(),
                "-r", WellKnownParticipant.DIFI_TEST.toString(),
                "-s", WellKnownParticipant.U4_TEST.toString(),
                "-t","true"
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

        OptionSet optionSet = optionParser.parse("-f","/tmp/dummy","-s", "9908:976098897","-r", "9908:810017902","-u", "https://ap.unit4.com", "-m", "as2");
        assertTrue(optionSet.has("u"));
        assertTrue(optionSet.has("f"));
    }

    @Test
    public void senderAndReceiverIsOptional() throws Exception {
        OptionParser optionParser = Main.getOptionParser();

        OptionSet optionSet = optionParser.parse("-f","/tmp/dummy","-u", "https://ap.unit4.com", "-m", "as2");
        assertFalse(optionSet.has("-r"));
        assertFalse(optionSet.has("-s"));
    }

}
