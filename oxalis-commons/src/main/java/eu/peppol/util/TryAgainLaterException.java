package eu.peppol.util;

import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: ebe
 * Date: 03.12.13
 * Time: 18:41
 * To change this template use File | Settings | File Templates.
 */
public class TryAgainLaterException extends RuntimeException {

    private final URL url;
    private final String retryAfter;

    public TryAgainLaterException(URL url, String retryAfter) {
        super("Service unavailable, try again at/after: " + retryAfter);

        this.url = url;
        this.retryAfter = retryAfter;
    }

    public URL getUrl() {
        return url;
    }

    public String getRetryAfter() {
        return retryAfter;
    }
}
