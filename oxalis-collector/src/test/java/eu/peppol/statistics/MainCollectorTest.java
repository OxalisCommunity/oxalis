package eu.peppol.statistics;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 10.04.13
 *         Time: 09:53
 */
public class MainCollectorTest {

    private OptionParser optionsParser;

    @BeforeMethod
    public void createTheOptionsParser() {
        optionsParser = MainCollector.createOptionsParser();

    }
    @Test
    public void testCreateOptionsParser() throws Exception {


        OptionSet optionSet = optionsParser.parse("--download", "--repository", System.getProperty("java.io.tmpdir"));

        assertTrue(optionSet.has("download"));
        assertTrue(optionSet.has("repository"));
        assertTrue(optionSet.hasArgument("repository"));
        File repository = (File) optionSet.valueOf("repository");
    }

    @Test
    public void downloadWithGivenListOfAccessPoints() {

        optionsParser.parse("--download", "--ap",  System.getProperty("java.io.tmpdir") + "aplist.csv","--repository", System.getProperty("java.io.tmpdir"));
    }

    @Test(expectedExceptions = OptionException.class)
    public void testMissingRepositoryOption() {
        optionsParser.parse("--download");
    }

    @Test
    public void testHelp() throws IOException {
        StringWriter stringWriter = new StringWriter();
        optionsParser.printHelpOn(stringWriter);
    }

    @Test
    public void helpPrintedIfNoArgumentsGiven() {
        OptionSet optionSet = optionsParser.parse();
        assertEquals(optionSet.specs().size(), 0);
    }
}
