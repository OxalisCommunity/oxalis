package eu.peppol.identifier;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a type safe PEPPOL Document Identifier, textually represented thus:
 * <pre>
 *     &lt;root NS>::&lt;document element local name>##&lt;customization id>::&lt;version>
 * </pre>
 *
 * @author Steinar Overbeck Cook
 * @author Thore Johnsen
 * @see "PEPPOL Policy for use of identifiers v3.0 of 2014-02-03"
 */
public class PeppolDocumentTypeId implements Serializable {

    String rootNameSpace;
    String localName;
    CustomizationIdentifier customizationIdentifier;
    String version;

    private static String scheme = "busdox-docid-qns";

    /**
     * <pre>
     *     &lt;root NS>::&lt;document element local name>##&lt;customization id>::&lt;version>
     * </pre>
     */
    static Pattern documentIdPattern = Pattern.compile("(urn:.*)::(.*)##(urn:.*)::(.*)");

    public PeppolDocumentTypeId(String rootNameSpace, String localName, CustomizationIdentifier customizationIdentifier, String version) {
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
        if (documentIdAsText != null) documentIdAsText = documentIdAsText.trim();
        Matcher matcher = documentIdPattern.matcher(documentIdAsText);
        if (matcher.matches()) {
            String rootNameSpace = matcher.group(1);
            String localName = matcher.group(2);
            String customizationIdAsText = matcher.group(3);
            String version = matcher.group(4);
            CustomizationIdentifier customizationIdentifier = CustomizationIdentifier.valueOf(customizationIdAsText);
            return new PeppolDocumentTypeId(rootNameSpace, localName, customizationIdentifier, version);
        } else
            throw new IllegalArgumentException("Unable to parse " + documentIdAsText + " into PEPPOL Document Type Identifier");
    }

    public static String getScheme() {
        return scheme;
    }

    /**
     * Provides a textual representation of this document type identifier
     *
     * @return textual value.
     */
    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder();
        sb.append(rootNameSpace);
        sb.append("::").append(localName);
        sb.append("##").append(customizationIdentifier);
        sb.append("::").append(version);
        return sb.toString();
    }

    public String toDebugString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DocumentTypeIdentifier");
        sb.append("{rootNameSpace='").append(rootNameSpace).append('\'');
        sb.append(", localName='").append(localName).append('\'');
        sb.append(", customizationIdentifier=").append(customizationIdentifier);
        sb.append(", version='").append(version).append('\'');
        sb.append('}');
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
        if (!customizationIdentifier.equals(that.customizationIdentifier)) return false;
        if (!localName.equals(that.localName)) return false;
        if (!rootNameSpace.equals(that.rootNameSpace)) return false;
        if (!version.equals(that.version)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = rootNameSpace.hashCode();
        result = 31 * result + localName.hashCode();
        result = 31 * result + customizationIdentifier.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
    
}
