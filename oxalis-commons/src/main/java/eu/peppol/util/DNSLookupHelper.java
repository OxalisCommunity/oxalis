package eu.peppol.util;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * User: Adam
 * Date: 6/3/13
 * Time: 2:35 PM
 */
public class DNSLookupHelper {

    /**
     * Checks if given domain exists
     *
     * InetAddress.getByName() tries to resolve name to an ip address and throws UnknownHostException if fails.
     *
     */
    public boolean domainExists(URL url) {

        try {
            InetAddress.getByName(url.getHost());
            return true;
        } catch (UnknownHostException exception) {
            return false;
        }
    }
}
