package eu.peppol.statistics.repository;

import eu.peppol.start.identifier.AccessPointIdentifier;

import javax.activation.MimeType;
import java.io.File;
import java.util.Date;

/**
 * Holds an entry in our repository of downloaded content, i.e. the "entity" of the Http response.
 *
 * @author steinar
 *         Date: 22.03.13
 *         Time: 08:51
 */
public class RepositoryEntry {

    private  File contentsFile;
    private  File metadataFile;

    RepositoryEntry(File contentsFile, File metadataFile){
        this.contentsFile = contentsFile;
        this.metadataFile = metadataFile;
    }

    RepositoryEntry(File rootDirectory, AccessPointIdentifier accessPointIdentifier, MimeType mimeType) {

        Date ts = new Date();

        contentsFile = FilenameGenerator.dataFile(rootDirectory, accessPointIdentifier, mimeType, ts);
        metadataFile = FilenameGenerator.metadataFile(rootDirectory, accessPointIdentifier, mimeType, ts);
    }

    RepositoryEntry() {

    }

    public void setContentsFile(File contentsFile) {
        this.contentsFile = contentsFile;
    }

    public void setMetadataFile(File metadataFile) {
        this.metadataFile = metadataFile;
    }

    public File getMetadataFile() {
        return metadataFile;
    }

    public File getContentsFile() {
        return contentsFile;
    }

}
