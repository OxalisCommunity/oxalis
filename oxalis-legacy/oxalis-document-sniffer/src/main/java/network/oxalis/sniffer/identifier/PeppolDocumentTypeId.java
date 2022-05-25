/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package network.oxalis.sniffer.identifier;

import network.oxalis.vefa.peppol.common.model.DocumentTypeIdentifier;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a type safe PEPPOL Document Identifier, textually represented thus:
 * <p>
 * <pre>
 *     {@literal <root NS>::<document element local name>##<customization id>::<version>}
 * </pre>
 *
 * @author Steinar Overbeck Cook
 * @author Thore Johnsen
 * @see "PEPPOL Policy for use of identifiers v3.0 of 2014-02-03"
 */
public class PeppolDocumentTypeId implements Serializable {

    private final String rootNameSpace;

    private final String localName;

    private final CustomizationIdentifier customizationIdentifier;

    private final String version;

    /**
     * <pre>
     *     &lt;root NS>::&lt;document element local name>##&lt;customization id>::&lt;version>
     * </pre>
     */
    static Pattern documentIdPattern = Pattern.compile("(.*)::(.*)##(.*)::(.*)");

    public PeppolDocumentTypeId(String rootNameSpace, String localName,
                                CustomizationIdentifier customizationIdentifier, String version) {
        this.rootNameSpace = rootNameSpace;
        this.localName = localName;
        this.customizationIdentifier = customizationIdentifier;
        this.version = version;
    }

    /**
     * Parses the supplied text string into the separate components of a PEPPOL Document Identifier.
     *
     * @param documentIdAsText textual representation of a document identifier.
     * @return type safe instance of DocumentTypeIdentifier
     */
    public static PeppolDocumentTypeId valueOf(String documentIdAsText) {
        if (documentIdAsText == null)
            throw new NullPointerException("Value 'null' is not a valid document type identifier.");

        Matcher matcher = documentIdPattern.matcher(documentIdAsText.trim());
        if (matcher.matches()) {
            String rootNameSpace = matcher.group(1);
            String localName = matcher.group(2);
            String customizationIdAsText = matcher.group(3);
            String version = matcher.group(4);
            CustomizationIdentifier customizationIdentifier = CustomizationIdentifier.valueOf(customizationIdAsText);
            return new PeppolDocumentTypeId(rootNameSpace, localName, customizationIdentifier, version);
        } else
            throw new IllegalArgumentException(
                    String.format("Unable to parseOld '%s' into PEPPOL Document Type Identifier", documentIdAsText));
    }

    /**
     * Provides a textual representation of this document type identifier
     *
     * @return textual value.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(rootNameSpace);
        sb.append("::").append(localName);
        sb.append("##").append(customizationIdentifier);
        sb.append("::").append(version);
        return sb.toString();
    }

    public String getRootNameSpace() {
        return rootNameSpace;
    }

    public String getLocalName() {
        return localName;
    }

    public CustomizationIdentifier getCustomizationIdentifier() {
        return customizationIdentifier;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeppolDocumentTypeId that = (PeppolDocumentTypeId) o;
        return Objects.equals(rootNameSpace, that.rootNameSpace) &&
                Objects.equals(localName, that.localName) &&
                Objects.equals(customizationIdentifier, that.customizationIdentifier) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootNameSpace, localName, customizationIdentifier, version);
    }

    public DocumentTypeIdentifier toVefa() {
        return DocumentTypeIdentifier.of(toString());
    }
}
