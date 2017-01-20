package eu.peppol.as2.outbound;

import brave.Span;
import brave.Tracer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.MessageSender;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.commons.tracing.Traceable;

class As2MessageSenderFacade extends Traceable implements MessageSender {

    private Provider<As2MessageSender> messageSenderProvider;

    @Inject
    public As2MessageSenderFacade(Tracer tracer, Provider<As2MessageSender> messageSenderProvider) {
        super(tracer);
        this.messageSenderProvider = messageSenderProvider;
    }

    @Override
    public TransmissionResponse send(TransmissionRequest transmissionRequest) throws OxalisTransmissionException {
        try (Span span = tracer.newTrace().name(getClass().getSimpleName()).start()) {
            return send(transmissionRequest, span);
        }
    }

    @Override
    public TransmissionResponse send(TransmissionRequest transmissionRequest, Span root) throws OxalisTransmissionException {
        return messageSenderProvider.get().send(transmissionRequest, root);
    }
}
