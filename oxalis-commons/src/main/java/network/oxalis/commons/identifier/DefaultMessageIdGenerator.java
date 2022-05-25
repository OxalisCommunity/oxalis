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

package network.oxalis.commons.identifier;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import network.oxalis.api.identifier.MessageIdGenerator;
import network.oxalis.api.inbound.InboundMetadata;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.util.Type;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author erlend
 * @since 4.0.4
 */
@Singleton
@Type("default")
public class DefaultMessageIdGenerator implements MessageIdGenerator {

    private String hostname;

    private AtomicLong atomicLong = new AtomicLong();

    @Inject
    public DefaultMessageIdGenerator(@Named("hostname") String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String generate(TransmissionRequest transmissionRequest) {
        return String.format("<%s.%s.%s.Oxalis@%s>", System.currentTimeMillis(),
                atomicLong.incrementAndGet(), transmissionRequest.hashCode(), hostname);
    }

    @Override
    public String generate(InboundMetadata inboundMetadata) {
        return String.format("<%s.%s.%s.Oxalis@%s>", System.currentTimeMillis(),
                atomicLong.incrementAndGet(), inboundMetadata.hashCode(), hostname);
    }
}
