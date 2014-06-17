package eu.peppol.util;

import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: ebe
 * Date: 03.12.13
 * Time: 18:34
 */
public class ConnectionException extends RuntimeException {

    private URL url;
    private int code;

    public ConnectionException(URL url, int code) {
        super("Error reading URL data (" + code + ") from " + url.toExternalForm());
        this.url = url;
        this.code = code;
    }

    public URL getUrl() {
        return url;
    }

    public int getCode() {
        return code;
    }

}
