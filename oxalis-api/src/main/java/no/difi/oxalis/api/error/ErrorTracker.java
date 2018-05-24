package no.difi.oxalis.api.error;

import no.difi.oxalis.api.model.Direction;

/**
 * Defining interface for tracking of exceptions received as result of external communication, both
 * inbound and outbound.
 *
 * @author erlend
 * @since 4.0.2
 */
public interface ErrorTracker {

    void track(Direction direction, Exception e);

}
