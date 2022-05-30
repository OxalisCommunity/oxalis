package network.oxalis.commons.error;

import network.oxalis.api.error.ErrorTracker;
import network.oxalis.api.model.Direction;
import network.oxalis.api.util.Type;

import javax.inject.Singleton;
import java.util.UUID;

/**
 * Silent error tracker with no logging and returning untracked identifiers.
 *
 * @author erlend
 * @since 4.0.2
 */
@Type("silent")
@Singleton
public class SilentErrorTracker implements ErrorTracker {

    @Override
    public String track(Direction direction, Exception e, boolean handled) {
        // No logging.
        return String.format("untracked:%s", UUID.randomUUID().toString());
    }
}
