package no.difi.oxalis.commons.error;

import no.difi.oxalis.api.error.ErrorTracker;
import no.difi.oxalis.api.model.Direction;
import no.difi.oxalis.api.util.Type;

import javax.inject.Singleton;

/**
 * @author erlend
 * @since 4.0.2
 */
@Type("noop")
@Singleton
public class NoopErrorTracker implements ErrorTracker {

    @Override
    public void track(Direction direction, Exception e) {
        // No action.
    }
}
