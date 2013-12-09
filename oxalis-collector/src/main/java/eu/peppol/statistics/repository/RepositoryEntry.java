package eu.peppol.statistics.repository;

import eu.peppol.identifier.AccessPointIdentifier;

import javax.activation.MimeType;
import java.io.File;
import java.util.Date;

/**
 * Holds an entry in our repository of downloaded content, i.e. the "entity" of the Http response.
 * An entry is represented by two files, the data and the meta data file.
 * <p>The data file holds the downloaded content, while the meta data file holds information about the URL from
 * which data was downloaded, the time stamp etc.</p>
 * <p>
 * Both files have the same name, but the meta data file has a an extension (file type) of {@link FilenameGenerator#META_DATA_SUFFIX}
 * </p>
 *
 * @author steinar
 *         Date: 22.03.13
 *         Time: 08:51
 */
public class RepositoryEntry {

    private AccessPointIdentifier accessPointIdentifier;
    private  File contentsFile;
    private  File metadataFile;

    RepositoryEntry(File contentsFile, File metadataFile){
        this.contentsFile = contentsFile;
        this.metadataFile = metadataFile;
    }

    RepositoryEntry(File rootDirectory, AccessPointIdentifier accessPointIdentifier, MimeType mimeType) {
        this.accessPointIdentifier = accessPointIdentifier;

        Date ts = new Date();

        contentsFile = FilenameGenerator.dataFile(rootDirectory, accessPointIdentifier, mimeType, ts);
        metadataFile = FilenameGenerator.metadataFile(rootDirectory, accessPointIdentifier, mimeType, ts);
    }

    RepositoryEntry() {

    }

    public AccessPointIdentifier getAccessPointIdentifier() {
        return accessPointIdentifier;
    }

    void setAccessPointIdentifier(AccessPointIdentifier accessPointIdentifier) {
        this.accessPointIdentifier = accessPointIdentifier;
    }

    void setContentsFile(File contentsFile) {
        this.contentsFile = contentsFile;
    }

    void setMetadataFile(File metadataFile) {
        this.metadataFile = metadataFile;
    }

    public File getMetadataFile() {
        return metadataFile;
    }

    public File getContentsFile() {
        return contentsFile;
    }

}
