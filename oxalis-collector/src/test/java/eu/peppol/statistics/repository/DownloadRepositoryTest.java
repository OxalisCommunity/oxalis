package eu.peppol.statistics.repository;

import eu.peppol.start.identifier.AccessPointIdentifier;
import eu.peppol.statistics.AccessPointMetaData;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 22.03.13
 *         Time: 08:41
 */
public class DownloadRepositoryTest {

    private DownloadRepository downloadRepository;
    private File rootDirectory;

    @BeforeTest
    public void setUp() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        assertTrue(tmpDir.getAbsolutePath() + " does not exist, is not a directory or is not writable", tmpDir.exists() && tmpDir.isDirectory());
        rootDirectory = new File(tmpDir, "oxalis");

        downloadRepository = new DownloadRepository(rootDirectory);

        System.out.println("Download repository in " + rootDirectory.getAbsolutePath());

    }

    @Test
    public void testSaveContents() throws Exception {

        String contents = "<note>\n" +
                "<to>Romeo</to>\n" +
                "<from>Julie</from>\n" +
                "<heading>Reminder</heading>\n" +
                "<body>Don't forget me this weekend!</body>\n" +
                "</note>";

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(contents.getBytes());

        AccessPointMetaData accessPointMetaData = new AccessPointMetaData(AccessPointIdentifier.TEST, "Ttest Company AS", "976098897", "Oxalis test company", new URL("https://aksesspunkt.sendregning.no?wsdl"));

        String metadata = accessPointMetaData.toString();

        RepositoryEntry repositoryEntry = downloadRepository.saveContents(AccessPointIdentifier.TEST, byteArrayInputStream, new MimeType("text/xml"), metadata);
        assertNotNull("No downloadedContents returned from repository", repositoryEntry);
        assertNotNull(repositoryEntry.getContentsFile());
        assertValidReadableFile(repositoryEntry.getContentsFile());

        String readContents = FileUtils.readFileToString(repositoryEntry.getContentsFile(), Charset.forName("UTF-8"));
        assertEquals(contents, readContents);

        String readMetadata = FileUtils.readFileToString(repositoryEntry.getMetadataFile(), Charset.forName("UTF-8"));
        assertEquals(metadata, readMetadata);
        System.out.println("");
    }

    /**
     * Attempts to list all the files in the repository and verifies that we are able to load the metadata together with
     * the actual contents of the entry.
     */
    @Test(dependsOnMethods = "testSaveContents")
    public void testFetchDownloadedContent() {
        Collection<RepositoryEntry> repositoryEntryEntries = downloadRepository.listDownloadedData();
        assertTrue("No files found in repository", repositoryEntryEntries.isEmpty() == false);

        for (RepositoryEntry entry : repositoryEntryEntries) {
            // The data file name is a subset of the the meta data file:
            //
            // 2013-03-22T11_29_55_496.xml          <<<<< Data
            // 2013-03-22T11_29_55_496.xml.meta.txt <<<<< Meta data
            assertTrue(entry.getMetadataFile().getAbsolutePath().startsWith(entry.getContentsFile().getAbsolutePath()));
        }
    }

    @Test(dependsOnMethods = "testSaveContents")
    public void testFileUtilsList() {
        Collection<File> files = FileUtils.listFiles(rootDirectory, TrueFileFilter.TRUE, TrueFileFilter.INSTANCE);
        assertTrue(files.isEmpty() == false);
    }

    void assertValidReadableFile(File contentsFile) {
        assertTrue(contentsFile.getAbsolutePath() + " does not exist", contentsFile.exists());
        assertTrue(contentsFile.getAbsolutePath() + " is not a file", contentsFile.isFile());
        assertTrue(contentsFile.getAbsolutePath() + " is not readable", contentsFile.canRead());
        assertTrue(contentsFile.getAbsolutePath() + " is empty", contentsFile.length() > 0);
    }


    @Test
    public void testArchiveDownloadedData() throws Exception {

        Collection<RepositoryEntry> repositoryEntries = downloadRepository.listDownloadedData();
        for (RepositoryEntry repositoryEntry : repositoryEntries) {
            downloadRepository.archive(repositoryEntry);
        }

        Collection<RepositoryEntry> listAfterArchiveOperations = downloadRepository.listDownloadedData();
        assertTrue(listAfterArchiveOperations.isEmpty());
    }
}
