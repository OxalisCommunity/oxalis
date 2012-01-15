package eu.peppol.start.identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: nigel
 * Date: Oct 7, 2011
 * Time: 7:23:20 PM
 */
public class Log {

    private static Logger log = LoggerFactory.getLogger("oxalis-com");

    public static void error(String s, Throwable throwable) {
        log.error(s, throwable);
    }

    public static void debug(String s) {
        log.debug(s);
    }

    public static void error(String s) {
        log.error(s);
    }

    public static void info(String s) {
        log.info(s);
    }

    public static void warn(String s) {
        log.warn(s);
    }
}
