/* Created by steinar on 20.05.12 at 13:02 */
package eu.peppol.start.identifier;

/**
 * Type safe value object holding a PEPPOL extension identifier.
 *
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class ExtensionIdentifier {
    private final String extensionIdValue;

    public ExtensionIdentifier(String extensionIdValue) {
        this.extensionIdValue = extensionIdValue;
    }

    public static ExtensionIdentifier valueFor(String extensionIdValue) {
        return new ExtensionIdentifier(extensionIdValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExtensionIdentifier that = (ExtensionIdentifier) o;

        if (!extensionIdValue.equals(that.extensionIdValue)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return extensionIdValue.hashCode();
    }

    @Override
    public String toString() {
        return extensionIdValue;
    }
}
