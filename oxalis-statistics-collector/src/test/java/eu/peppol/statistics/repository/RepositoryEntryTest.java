package eu.peppol.statistics.repository;

import eu.peppol.identifier.AccessPointIdentifier;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 22.03.13
 *         Time: 10:36
 */
public class RepositoryEntryTest {

    @Test
    public void testCreate() throws Exception {

        RepositoryEntry repositoryEntry = new RepositoryEntry(new File("/"), AccessPointIdentifier.TEST, new MimeType("text/xml"));

        File contentsFile = repositoryEntry.getContentsFile();
        File metadataFile = repositoryEntry.getMetadataFile();

        // Both files should reside in same directory
        assertEquals(contentsFile.getParentFile(), metadataFile.getParentFile());

        // File names should not be null
        assertNotNull(contentsFile.getName());
        assertNotNull(metadataFile.getName());

        // Filename should not be equal
        assertNotEquals(contentsFile.getName(), metadataFile.getName());
    }
}
