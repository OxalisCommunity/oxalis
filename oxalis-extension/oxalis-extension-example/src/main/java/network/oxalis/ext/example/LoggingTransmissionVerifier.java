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

package network.oxalis.ext.example;

import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.model.Direction;
import network.oxalis.api.transmission.TransmissionVerifier;
import network.oxalis.api.util.Type;
import network.oxalis.vefa.peppol.common.model.Header;

import javax.inject.Singleton;

/**
 * This is a simple implementation of {@link TransmissionVerifier} where each message is logged.
 *
 * @author erlend
 * @since 4.0.1
 */
@Slf4j
@Singleton
@Type("logging") // Name given to the implementation for use in configuration.
public class LoggingTransmissionVerifier implements TransmissionVerifier {

    @Override
    public void verify(Header header, Direction direction) {
        log.info("Direction: {} | Sender/Receiver: {}/{} | Instance identifier: {}",
                direction,
                header.getSender().getIdentifier(),
                header.getReceiver().getIdentifier(),
                header.getIdentifier());
    }
}
