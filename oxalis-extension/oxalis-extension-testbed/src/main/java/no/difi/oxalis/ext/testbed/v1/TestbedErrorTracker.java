package no.difi.oxalis.ext.testbed.v1;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import no.difi.oxalis.api.error.ErrorTracker;
import no.difi.oxalis.api.model.Direction;
import no.difi.oxalis.ext.testbed.v1.jaxb.ErrorType;

/**
 * @author erlend
 */
@Singleton
public class TestbedErrorTracker implements ErrorTracker {

    @Inject
    private TestbedSender sender;

    @Override
    public String track(Direction direction, Exception e, boolean handled) {
        ErrorType error = new ErrorType();
        error.setMessage(e.getMessage());
        sender.send(error);

        return null;
    }
}
