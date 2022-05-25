/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.as2.outbound;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.opentracing.Span;
import io.opentracing.Tracer;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.outbound.MessageSender;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.outbound.TransmissionResponse;
import network.oxalis.commons.tracing.Traceable;

class As2MessageSenderFacade extends Traceable implements MessageSender {

    private Provider<As2MessageSender> messageSenderProvider;

    @Inject
    public As2MessageSenderFacade(Tracer tracer, Provider<As2MessageSender> messageSenderProvider) {
        super(tracer);
        this.messageSenderProvider = messageSenderProvider;
    }

    @Override
    public TransmissionResponse send(TransmissionRequest transmissionRequest) throws OxalisTransmissionException {
        Span span = tracer.buildSpan(getClass().getSimpleName()).start();
        try {
            return send(transmissionRequest, span);
        } finally {
            span.finish();
        }
    }

    @Override
    public TransmissionResponse send(TransmissionRequest transmissionRequest, Span root)
            throws OxalisTransmissionException {
        return messageSenderProvider.get().send(transmissionRequest, root);
    }
}
