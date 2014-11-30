package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.Injector;

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
        TransmissionRequestBuilder builder = injector.getInstance(TransmissionRequestBuilder.class);
        builder.allowOverride = true;
        return builder;
    }

    public TransmissionRequestBuilder createTansmissionRequestBuilderNotAllowingOverrides() {
        TransmissionRequestBuilder builder = injector.getInstance(TransmissionRequestBuilder.class);
        builder.allowOverride = false;
        return builder;
    }

}
