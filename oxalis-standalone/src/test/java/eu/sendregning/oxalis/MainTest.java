package eu.sendregning.oxalis;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 08.11.13
 *         Time: 14:17
 */
public class MainTest {

    @Test
    public void testGetOptionParser() throws Exception {
        OptionParser optionParser = Main.getOptionParser();

        OptionSet optionSet = optionParser.parse("-f","/tmp/dummy","-s", "9908:976098897","-r", "9908:810017902","-u", "https://ap.unit4.com", "-m", "as2");
        assertTrue(optionSet.has("u"));
        assertTrue(optionSet.has("f"));

    }
}
