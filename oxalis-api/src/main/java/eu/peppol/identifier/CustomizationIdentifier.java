package eu.peppol.identifier;

/**
 * Represents a PEPPOL Customization Identifier contained within a PEPPOL Document Identifier.
 *
 * @author Steinar Overbeck Cook steinar@sendregning.no
 * @author Thore Johnsen thore@sendregning.no
 *
 * @see "PEPPOL Policy for use of identifiers v3.0 of 2014-02-03"
 */
public class CustomizationIdentifier {

    private String value;

    public CustomizationIdentifier(String customizationIdentifier) {
        if (customizationIdentifier != null) customizationIdentifier = customizationIdentifier.trim();
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
        return value.equals(that.value);
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
