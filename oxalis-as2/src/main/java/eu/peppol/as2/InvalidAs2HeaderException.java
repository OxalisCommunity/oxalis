package eu.peppol.as2;

/**
 * @author steinar
 *         Date: 09.10.13
 *         Time: 13:37
 */
public class InvalidAs2HeaderException extends InvalidAs2MessageException {

    private final As2Header headerName;
    private final String value;

    public InvalidAs2HeaderException(As2Header headerName, String value) {
        super("Invalid value for As2Header " + headerName + ": '" + value + "'");
        this.headerName = headerName;
        this.value = value;
    }

    public As2Header getHeaderName() {
        return headerName;
    }

    public String getValue() {
        return value;
    }
}
