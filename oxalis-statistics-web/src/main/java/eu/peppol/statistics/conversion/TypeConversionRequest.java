package eu.peppol.statistics.conversion;

/**
 * @author steinar
 *         Date: 22.04.13
 *         Time: 13:35
 */
public class TypeConversionRequest {
    private final String label;
    private final String stringValue;

    public TypeConversionRequest(String label, String stringValue) {

        this.label = label;
        this.stringValue = stringValue;
    }

    String getLabel() {
        return label;
    }

    String getStringValue() {
        return stringValue;
    }
}
