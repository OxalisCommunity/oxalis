package eu.peppol.as2;

/**
 * @author steinar
 *         Date: 09.10.13
 *         Time: 13:38
 */
public class InvalidAs2MessageException extends Exception {

    public InvalidAs2MessageException(String s) {
        super(s);
    }

    public InvalidAs2MessageException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
