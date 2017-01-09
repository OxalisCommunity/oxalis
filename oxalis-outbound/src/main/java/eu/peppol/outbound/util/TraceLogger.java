package eu.peppol.outbound.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceLogger {

    private Logger logger;

    public static TraceLogger getLogger(Class<?> clazz) {
        return new TraceLogger(clazz);
    }

    private TraceLogger(Class<?> clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    public void debug(Trace trace, String s, Object... objects) {
        if (trace != null)
            logger.debug("{} " + s, trace, objects);
    }

    public void info(Trace trace, String s, Object... objects) {
        if (trace != null)
            logger.info("{} " + s, trace, objects);
    }

    public void warn(Trace trace, String s, Object... objects) {
        if (trace != null)
            logger.warn("{} " + s, trace, objects);
    }

    public void error(Trace trace, String s, Object... objects) {
        if (trace != null)
            logger.error("{} " + s, trace, objects);
    }
}
