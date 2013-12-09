package eu.peppol.as2;

/**
* @author steinar
*         Date: 21.10.13
*         Time: 17:00
*/
public class InvalidAs2SystemIdentifierException extends InvalidAs2MessageException {

    private final String as2Name;

    public InvalidAs2SystemIdentifierException(String as2Name) {
        super("Invalid AS2 System Identifier: " + as2Name);
        this.as2Name = as2Name;
    }

    private String getAs2Name() {
        return as2Name;
    }
}
