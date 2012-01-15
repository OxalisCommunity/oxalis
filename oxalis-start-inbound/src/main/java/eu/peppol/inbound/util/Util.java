package eu.peppol.inbound.util;

/**
 * User: nigel
 * Date: Oct 28, 2011
 * Time: 11:14:54 AM
 */
public class Util {

    public static void logAndThrowRuntimeException(String context, Exception e) {
        Log.error(context, e);
        throw new RuntimeException(context, e);
    }

}
