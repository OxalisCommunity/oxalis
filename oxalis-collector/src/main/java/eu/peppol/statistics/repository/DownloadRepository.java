package eu.peppol.statistics.repository;

import eu.peppol.start.identifier.AccessPointIdentifier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import javax.activation.MimeType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles operations related to persistent storage of downloaded contents from the access points.
 *
 * @author steinar
 *         Date: 21.03.13
 *         Time: 17:25
 */
public class DownloadRepository {

    private final File rootDirectory;
    private final File downloadedDataDirectory;
    private final File archivedDataDirectory;

    public DownloadRepository(File rootDirectory) {
        this.rootDirectory = rootDirectory;
        downloadedDataDirectory = new File(rootDirectory, "data");
        archivedDataDirectory = new File(rootDirectory, "archive");
    }


    // Persists downloaded contents
    public RepositoryEntry saveContents(AccessPointIdentifier accessPointIdentifier, InputStream inputStream, MimeType mimeType, String metadata) {

        // Figures out which file to save data into
        RepositoryEntry repositoryEntry = new RepositoryEntry(downloadedDataDirectory, accessPointIdentifier, mimeType);

        // Writes the data from the input stream
        try {
            FileUtils.copyInputStreamToFile(inputStream, repositoryEntry.getContentsFile());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write data to " + repositoryEntry.getContentsFile().getAbsolutePath());
        }

        try {
            FileUtils.writeStringToFile(repositoryEntry.getMetadataFile(), metadata, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write meta data information to " + repositoryEntry.getMetadataFile());
        }

        return repositoryEntry;
    }

    public Collection<RepositoryEntry> listDownloadedData() {

        Collection<File> files = FileUtils.listFiles(downloadedDataDirectory, TrueFileFilter.TRUE, TrueFileFilter.INSTANCE);

        Map<String, RepositoryEntry> contentFiles = new HashMap<String, RepositoryEntry>();

        for (File file : files) {
            String filenameWithoutExtension = FilenameGenerator.getFilenameWithoutExtension(file);
            RepositoryEntry downloadContentFile;

            if (contentFiles.containsKey(filenameWithoutExtension)) {
                downloadContentFile = contentFiles.get(filenameWithoutExtension);
            } else {
                downloadContentFile = new RepositoryEntry();
                contentFiles.put(filenameWithoutExtension, downloadContentFile);
            }

            if (FilenameGenerator.isMetadataFile(file)) {
                downloadContentFile.setMetadataFile(file);
            } else {
                downloadContentFile.setContentsFile(file);
            }
        }

        return contentFiles.values();
    }


    public void archive(RepositoryEntry repositoryEntry) {
        archiveFile(repositoryEntry.getContentsFile());
        archiveFile(repositoryEntry.getMetadataFile());
    }


    private void archiveFile(File file) {
        File archiveFilename = FilenameGenerator.filenameForMoveToAnotherDirectory(rootDirectory, archivedDataDirectory, file);
        try {
            FileUtils.moveFile(file, archiveFilename);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to move file from " + file.getAbsolutePath() + " to " + archiveFilename);
        }
    }
}
