package eu.peppol.statistics.repository;

import eu.peppol.start.identifier.AccessPointIdentifier;
import eu.peppol.statistics.AccessPointMetaData;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.joda.time.DateTime;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.regex.Matcher;

import static org.testng.Assert.*;

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
        assertTrue(tmpDir.exists() && tmpDir.isDirectory(),tmpDir.getAbsolutePath() + " does not exist, is not a directory or is not writable");
        rootDirectory = new File(tmpDir, "oxalis");

        downloadRepository = new DownloadRepository(rootDirectory);
    }

    @AfterTest
    public void removeDirectory(){

        rootDirectory.delete();
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

        AccessPointMetaData accessPointMetaData = new AccessPointMetaData(AccessPointIdentifier.TEST, "Test Company AS", "976098897", "Oxalis test company", new URL("https://aksesspunkt.sendregning.no/oxalis/accessPointService?wsdl"), new URL("https://aksesspunkt.sendregning.no/oxalis/statistics"));

        String metadata = accessPointMetaData.toString();

        MimeType mimeTypeOfDownloadedContents = new MimeType("text/xml");

        // Computes the name of the file holding the time stamp of the last successful save of contents
        File tstampFile = FilenameGenerator.timestampFileFor(downloadRepository.getRootDirectory(), AccessPointIdentifier.TEST);

        // Sets flag to indicate whether there was a previous time stamp file in the repository
        boolean timestampFileExistedBeforeTest = tstampFile.canRead() ? true : false;
        DateTime dateTimeBeforeSaveOfContents = downloadRepository.fetchLastTimeStamp(AccessPointIdentifier.TEST);

        RepositoryEntry repositoryEntry = downloadRepository.saveContents(AccessPointIdentifier.TEST, byteArrayInputStream, mimeTypeOfDownloadedContents, metadata);

        assertNotNull(repositoryEntry,"No downloadedContents returned from repository");
        assertNotNull(repositoryEntry.getContentsFile());
        assertValidReadableFile(repositoryEntry.getContentsFile());

        // Verifies that the contents file contains the data we wrote into it
        String readContents = FileUtils.readFileToString(repositoryEntry.getContentsFile(), Charset.forName("UTF-8"));
        assertEquals(contents, readContents);

        // Verifies that the meta data file contains the data we passed to the repository
        String readMetadata = FileUtils.readFileToString(repositoryEntry.getMetadataFile(), Charset.forName("UTF-8"));
        assertEquals(metadata, readMetadata);

        // Verifies that the timestamp file was updated
        DateTime lastSuccess = downloadRepository.fetchLastTimeStamp(AccessPointIdentifier.TEST);
        assertNotNull(lastSuccess, "No timestamp found for last successful save of contents");

        if (timestampFileExistedBeforeTest) {
            assertNotEquals(dateTimeBeforeSaveOfContents, lastSuccess);
        }
    }


    /**
     * Attempts to list all the files in the repository and verifies that we are able to load the metadata together with
     * the actual contents of the entry.
     */
    @Test(dependsOnMethods = "testSaveContents")
    public void testFetchDownloadedContent() {
        Collection<RepositoryEntry> repositoryEntryEntries = downloadRepository.listDownloadedData();
        assertTrue( repositoryEntryEntries.isEmpty() == false,"No files found in repository: " + downloadRepository.getRootDirectory().getAbsolutePath());


        for (RepositoryEntry entry : repositoryEntryEntries) {
            // The data file name is a subset of the the meta data file:
            //
            // 2013-03-22T11_29_55_496.xml          <<<<< Data
            // 2013-03-22T11_29_55_496.xml.meta.txt <<<<< Meta data
            assertTrue(entry.getMetadataFile().getAbsolutePath().startsWith(entry.getContentsFile().getAbsolutePath()));
            Matcher matcher = FilenameGenerator.filenamePattern.matcher(entry.getContentsFile().getName());
            assertTrue(matcher.find());

            assertNotNull(entry.getAccessPointIdentifier());
        }
    }

    /**
     * Experiments with the Apache Commons IO library
     */
    @Test(dependsOnMethods = "testSaveContents")
    public void testFileUtilsList() {

        // List all files within the data directory, the supplied filter ensures that we do not get any files
        // with extension ".log" returned.
        Collection<File> files = FileUtils.listFiles(rootDirectory, new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.getAbsolutePath().endsWith(".log")) {
                    return false;
                } else return true;
            }

            @Override
            public boolean accept(File file, String s) {
                return true;
            }
        }, TrueFileFilter.INSTANCE);

        assertTrue(files.isEmpty() == false);

        // No files with .log extension
        for (File file : files) {
            assertFalse(file.getAbsolutePath().endsWith(".log"));
        }
    }


    void assertValidReadableFile(File contentsFile) {
        assertTrue(contentsFile.exists(),contentsFile.getAbsolutePath() + " does not exist");
        assertTrue(contentsFile.isFile(), contentsFile.getAbsolutePath() + " is not a file");
        assertTrue(contentsFile.canRead(), contentsFile.getAbsolutePath() + " is not readable");
        assertTrue(contentsFile.length() > 0, contentsFile.getAbsolutePath() + " is empty");
    }


    /**
     * Tests the archival of the downloaded data.
     *
     * @throws Exception
     */
    @Test(dependsOnMethods = "testFetchDownloadedContent")
    public void testArchiveDownloadedData() throws Exception {

        Collection<RepositoryEntry> repositoryEntries = downloadRepository.listDownloadedData();
        for (RepositoryEntry repositoryEntry : repositoryEntries) {
            downloadRepository.archive(repositoryEntry);
        }

        Collection<RepositoryEntry> listAfterArchiveOperations = downloadRepository.listDownloadedData();
        assertTrue(listAfterArchiveOperations.isEmpty());
    }
}
