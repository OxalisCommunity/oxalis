package eu.peppol.statistics.repository;

import eu.peppol.start.identifier.AccessPointIdentifier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.joda.time.DateTime;

import javax.activation.MimeType;
import javax.print.attribute.standard.DateTimeAtCompleted;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Handles operations related to persistent storage of downloaded contents from the access points. The downloaded
 * files are stored in the file system. Each entry is represented by a {@link RepositoryEntry} instance.
 *
 * @author steinar
 *         Date: 21.03.13
 *         Time: 17:25
 */
public class DownloadRepository {

    private final File rootDirectory;
    private final File downloadedDataDirectory;
    private final File archivedDataDirectory;

    public File getRootDirectory() {
        return rootDirectory;
    }

    public File getDownloadedDataDirectory() {
        return downloadedDataDirectory;
    }

    public File getArchivedDataDirectory() {
        return archivedDataDirectory;
    }

    public DownloadRepository(File rootDirectory) {
        this.rootDirectory = rootDirectory;
        downloadedDataDirectory = new File(rootDirectory, "data");
        downloadedDataDirectory.mkdirs();
        archivedDataDirectory = new File(rootDirectory, "archive");
        archivedDataDirectory.mkdirs();
    }


    // Persists downloaded contents
    public RepositoryEntry saveContents(AccessPointIdentifier accessPointIdentifier, InputStream inputStream, MimeType mimeType, String metadata) {

        // Figures out which directory and files to save data and meta data into
        RepositoryEntry repositoryEntry = new RepositoryEntry(downloadedDataDirectory, accessPointIdentifier, mimeType);

        // Writes the data from the input stream
        try {
            FileUtils.copyInputStreamToFile(inputStream, repositoryEntry.getContentsFile());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write data to " + repositoryEntry.getContentsFile().getAbsolutePath());
        }

        // Saves the meta data
        try {
            FileUtils.writeStringToFile(repositoryEntry.getMetadataFile(), metadata, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write meta data information to " + repositoryEntry.getMetadataFile());
        }


        // Saves the time stamp of this successful operation
        createOrUpdateTimestamp(accessPointIdentifier);
        return repositoryEntry;
    }


    void createOrUpdateTimestamp(AccessPointIdentifier accessPointIdentifier) {
        File tstampFile = FilenameGenerator.timestampFileFor(rootDirectory, accessPointIdentifier);
        try {
            DateTime dateTime = new DateTime();
            FileUtils.write(tstampFile, dateTime.toString());
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(tstampFile.getAbsolutePath() + " " + e, e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write timestamp to " + tstampFile.getAbsolutePath());
        }
    }

    public DateTime fetchLastTimeStamp(AccessPointIdentifier accessPointIdentifier) {
        File tstampFile = FilenameGenerator.timestampFileFor(rootDirectory, accessPointIdentifier);

        DateTime result = null;
        if (tstampFile.exists() && tstampFile.canRead()) {
            try {
                String data = FileUtils.readFileToString(tstampFile);
                result = new DateTime(data);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read data from " + tstampFile.getAbsolutePath(), e);
            }
        }

        return result;
    }



    /**
     * Lists all files having a name starting with a ISO8601 timestamp.
     *
     * @return
     */
    public Collection<RepositoryEntry> listDownloadedData() {

        if (!downloadedDataDirectory.exists() || !downloadedDataDirectory.isDirectory()) {
            throw new IllegalStateException("Directory " + downloadedDataDirectory.getAbsolutePath() + " does not exist or is not a directory");
        }
        Collection<File> files = FileUtils.listFiles(downloadedDataDirectory, new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.getName().matches(FilenameGenerator.ISO8601_TIMESTAMP_PATTERN))
                    return true;
                else
                    return false;
            }

            @Override
            public boolean accept(File file, String s) {
                return false;
            }
        }, TrueFileFilter.INSTANCE);

        Map<String, RepositoryEntry> resultsMap = new HashMap<String, RepositoryEntry>();

        for (File file : files) {
            String filenameWithoutExtension = FilenameGenerator.getFilenameWithoutExtension(file);
            RepositoryEntry downloadContentFile;

            // Locates the entry holding the RepositoryEntry for this entry or creates a new one.
            if (resultsMap.containsKey(filenameWithoutExtension)) {
                downloadContentFile = resultsMap.get(filenameWithoutExtension);
            } else {
                // Not found, creates it.
                downloadContentFile = new RepositoryEntry();
                resultsMap.put(filenameWithoutExtension, downloadContentFile);
            }

            if (FilenameGenerator.isMetadataFile(file)) {
                downloadContentFile.setMetadataFile(file);
            } else {
                downloadContentFile.setContentsFile(file);
            }
        }

        return resultsMap.values();
    }


    /** Archives the files related to the given repository entry in the "archive" directory, by simply
     * moving them from the "data" directory.
     *
     * @param repositoryEntry represents the collection of files to be archived
     */
    public void archive(RepositoryEntry repositoryEntry) {
        archiveFile(repositoryEntry.getContentsFile());
        archiveFile(repositoryEntry.getMetadataFile());
    }


    /** Archives a single file */
    private void archiveFile(File file) {
        File archiveFilename = FilenameGenerator.filenameForMoveToAnotherDirectory(downloadedDataDirectory, archivedDataDirectory, file);
        try {
            FileUtils.moveFile(file, archiveFilename);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to move file from " + file.getAbsolutePath() + " to " + archiveFilename);
        }
    }

}
