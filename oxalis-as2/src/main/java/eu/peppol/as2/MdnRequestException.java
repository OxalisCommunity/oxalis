package eu.peppol.as2;

/**
 * Indicates that the MDN request could not be handled. I.e. the requested protocol is not available or there
 * was an error during the parsing of the header "disposition-notification-options"
 *
 * @author steinar
 *         Date: 17.10.13
 *         Time: 22:27
 */
public class MdnRequestException extends Exception {

    public MdnRequestException(String msg) {
        super(msg);
    }
}
