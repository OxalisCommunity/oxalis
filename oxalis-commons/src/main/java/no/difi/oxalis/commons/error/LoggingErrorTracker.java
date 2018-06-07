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

package no.difi.oxalis.commons.error;

import no.difi.oxalis.api.error.ErrorTracker;
import no.difi.oxalis.api.model.Direction;
import no.difi.oxalis.api.util.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * @author erlend
 * @since 4.0.2
 */
@Type("logging")
@Singleton
public class LoggingErrorTracker implements ErrorTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingErrorTracker.class);

    @Override
    public void track(Direction direction, Exception e) {
        LOGGER.warn(e.getMessage(), e);
    }
}
