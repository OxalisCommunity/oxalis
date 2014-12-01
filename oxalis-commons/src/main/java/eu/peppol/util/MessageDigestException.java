/* Created by steinar on 18.05.12 at 13:32 */
package eu.peppol.util;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class MessageDigestException extends Exception {

    String inputValue;

    public MessageDigestException(String value, Exception e) {
        super("Unable to digest " + value + "; " + e.getMessage(), e);
        inputValue = value;
    }

    public String getInputValue() {
        return inputValue;
    }

}
