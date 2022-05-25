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

package network.oxalis.commons.timestamp;

import com.google.inject.Singleton;
import network.oxalis.api.model.Direction;
import network.oxalis.api.timestamp.Timestamp;
import network.oxalis.api.timestamp.TimestampProvider;
import network.oxalis.api.util.Type;

import java.util.Date;

/**
 * Implementation of {@link TimestampProvider} simply providing timestamps using internal computer clock.
 *
 * @author erlend
 * @since 4.0.0
 */
@Singleton
@Type("system")
class SystemTimestampProvider implements TimestampProvider {

    @Override
    public Timestamp generate(byte[] content, Direction direction) {
        return new Timestamp(new Date(), null);
    }
}
