package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.Injector;
import eu.peppol.util.GlobalState;

/**
 * Injectable helper that creates TransmissionRequestBuilder's running in "TEST" mode
 * (allows for overriding sender, receiever, document and profile)
 *
 * Instances of this class must be injected to work
 *
 * @author thore
 */
public class OverridableTransmissionRequestBuilderCreator {

    @Inject
    Injector injector;

    public TransmissionRequestBuilder createTansmissionRequestBuilderAllowingOverrides() {
        GlobalState.getInstance().setTransmissionBuilderOverride(true);
        TransmissionRequestBuilder builder = injector.getInstance(TransmissionRequestBuilder.class);
        return builder;
    }

    public TransmissionRequestBuilder createTansmissionRequestBuilderNotAllowingOverrides() {
        GlobalState.getInstance().setTransmissionBuilderOverride(false);
        TransmissionRequestBuilder builder = injector.getInstance(TransmissionRequestBuilder.class);
        return builder;
    }

}
