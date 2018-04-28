/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package no.difi.oxalis.outbound.transmission;

import brave.Span;
import brave.Tracer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.outbound.TransmissionService;
import no.difi.oxalis.api.outbound.Transmitter;
import no.difi.oxalis.commons.tracing.Traceable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Default implementation of {@link TransmissionService}.
 *
 * @author erlend
 */
@Singleton
class DefaultTransmissionService extends Traceable implements TransmissionService {

    private final TransmissionRequestFactory transmissionRequestFactory;

    private final Transmitter transmitter;

    /**
     * {@inheritDoc}
     */
    @Inject
    public DefaultTransmissionService(TransmissionRequestFactory transmissionRequestFactory,
                                      Transmitter transmitter, Tracer tracer) {
        super(tracer);
        this.transmissionRequestFactory = transmissionRequestFactory;
        this.transmitter = transmitter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransmissionResponse send(InputStream inputStream)
            throws IOException, OxalisTransmissionException, OxalisContentException {
        Span root = tracer.newTrace().name("TransmissionService").start();
        try {
            return send(inputStream, root);
        } finally {
            root.finish();
        }
    }

    @Override
    public TransmissionResponse send(InputStream inputStream, Span root)
            throws IOException, OxalisTransmissionException, OxalisContentException {
        return transmitter.transmit(transmissionRequestFactory.newInstance(inputStream, root), root);
    }
}
