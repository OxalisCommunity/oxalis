package eu.peppol.persistence.file;

/**
 * Describes the various files being persisted and managed during transmission.
 *
 * @author steinar
 *         Date: 21.10.2016
 *         Time: 16.43
 */
public enum ArtifactType {

    /**
     * The main document, i.e the invoice, purchase order, etc.
     */
    PAYLOAD("The payload", "PAYLOAD_URL", "-doc.xml"),

    /**
     * The transport protocol specific native evidene, i.e. MDN for AS2 protocol
     */
    NATIVE_EVIDENCE("Protocol specific transmission evidence", "NATIVE_EVIDENCE_URL", "-rcpt.smime");

    private final String description;

    /** Name of database column in MESSAGE table holding the url of the artifact */
    private final String columnName;
    private final String fileNameSuffix;

    ArtifactType(String description, String columnName, String fileNameSuffix) {
        this.description = description;
        this.columnName = columnName;
        this.fileNameSuffix = fileNameSuffix;
    }

    public String getDescription() {
        return description;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getFileNameSuffix() {
        return fileNameSuffix;
    }
}
