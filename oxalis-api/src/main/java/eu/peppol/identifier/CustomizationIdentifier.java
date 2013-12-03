/* Created by steinar on 20.05.12 at 12:07 */
package eu.peppol.identifier;

/**
 * Represents a PEPPOL Customization Identifier contained within a PEPPOL  Document Identifier, for which the
 * pattern is
 * <pre>
 *     &lt;transactionId>:#&lt;extensionId>[#&lt;extensionId>]
 * </pre>
 * I.e. a string followed by ":#" followed by another string followed by an optional string starting with "#"
 * and followed by another string.
 *
 * @author Steinar Overbeck Cook steinar@sendregning.no
 * @see "PEPPOL Policy for us of Identifiers v2.2"
 */
public class CustomizationIdentifier {

    private String value;

    public CustomizationIdentifier(String customizationIdentifier) {
        this.value = customizationIdentifier;
    }

    public static CustomizationIdentifier valueOf(String s) {
        return new CustomizationIdentifier(s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomizationIdentifier that = (CustomizationIdentifier) o;

        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
