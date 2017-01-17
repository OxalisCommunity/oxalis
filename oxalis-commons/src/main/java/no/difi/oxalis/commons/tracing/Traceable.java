package no.difi.oxalis.commons.tracing;

import brave.Tracer;

/**
 * Simple abstract class making {@link Tracer} object available in a standardized manner for classes extending this
 * class.
 *
 * @author erlend
 * @since 4.0.0
 */
public abstract class Traceable {

    /**
     * Zipkin tracer implementation.
     */
    protected final Tracer tracer;

    /**
     * Default constructor accepting a tracer.
     *
     * @param tracer Tracer from application context.
     */
    protected Traceable(Tracer tracer) {
        this.tracer = tracer;
    }
}
