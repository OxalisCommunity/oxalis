/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

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
