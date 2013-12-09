package eu.peppol.statistics.repository;

import eu.peppol.identifier.AccessPointIdentifier;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import java.io.File;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 22.03.13
 *         Time: 10:13
 */
public class FilenameGeneratorTest {
    @Test
    public void testFilenameFromTimeStamp() throws Exception {

        String s = FilenameGenerator.filenameFromTimeStamp(new Date());
    }

    @Test
    public void testMetadataFilename() throws Exception {
        File f = FilenameGenerator.metadataFile(new File("/"), AccessPointIdentifier.TEST, new MimeType("text/xml"), new Date());

        String extension = getExtension(f);
        assertEquals(extension, ".txt");

    }

    @Test
    public void testGetFilenameWithoutExtension() {
        String filenameOnly = "2013-03-22T11_22_55_496";
        File f = new File("/var/folders/1x/asdfasdf/T/NO-TEST/" + filenameOnly + ".xml.meta.txt");

        String filenameWithoutExtension = FilenameGenerator.getFilenameWithoutExtension(f);
        assertEquals(filenameWithoutExtension, filenameOnly);
    }

    @Test
    public void testRenameFile() throws Exception {

        File data = new File("/tmp/data");
        File archive = new File("/tmp/archive");
        File fileToBeRenamed = new File(data.getAbsolutePath() + "/99/sample.txt");

        File newFile = FilenameGenerator.filenameForMoveToAnotherDirectory(data, archive, fileToBeRenamed);

        assertEquals(newFile, new File(archive.getAbsolutePath() + "/99/sample.txt"));

    }


    @Test
    public void nameOfParentDirectory() {
        String apId = "SR-TEST";
        File file = new File("/tmp/data/" + apId + "/ole.txt");
        String parentDirName = FilenameGenerator.getParentDirName(file);
        assertEquals(parentDirName, apId);
    }


    private String getExtension(File f) {
        Pattern pattern = Pattern.compile("(\\.[^.]*)$");
        Matcher matcher = pattern.matcher(f.getAbsolutePath());
        assertTrue(matcher.find());
        return matcher.group(1);
    }
}
