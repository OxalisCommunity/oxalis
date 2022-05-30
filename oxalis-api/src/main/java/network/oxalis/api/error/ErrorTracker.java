package network.oxalis.api.error;

import network.oxalis.api.model.Direction;

/**
 * Defining interface for tracking of exceptions received as result of external communication, both
 * inbound and outbound.
 *
 * @author erlend
 * @since 4.0.2
 */
public interface ErrorTracker {

    /**
     * Method called where errors are gathered.
     *
     * @param direction Direction of transmission where error occurred.
     * @param e The exception triggered.
     * @param handled Whether Oxalis were able to gracefully handle the exception using own relevant exception handling.
     * @return Identifier uniquely identifying the error in the error handling system or logging.
     * @since 4.0.2
     */
    String track(Direction direction, Exception e, boolean handled);

}
