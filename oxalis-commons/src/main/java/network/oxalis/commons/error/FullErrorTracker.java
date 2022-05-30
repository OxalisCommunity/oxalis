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

package network.oxalis.commons.error;

import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.error.ErrorTracker;
import network.oxalis.api.model.Direction;
import network.oxalis.api.util.Type;

import javax.inject.Singleton;
import java.util.UUID;

/**
 * @author erlend
 * @since 4.0.2
 */
@Slf4j
@Type("full")
@Singleton
public class FullErrorTracker implements ErrorTracker {

    @Override
    public String track(Direction direction, Exception e, boolean handled) {
        String identifier = UUID.randomUUID().toString();

        if (handled)
            log.warn("[{}] {}", identifier, e.getMessage(), e);
        else
            log.error("[{}] {}", identifier, e.getMessage(), e);

        return identifier;
    }
}
