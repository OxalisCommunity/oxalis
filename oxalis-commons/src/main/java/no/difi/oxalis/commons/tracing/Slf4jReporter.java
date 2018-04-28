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

package no.difi.oxalis.commons.tracing;

import com.google.inject.Singleton;
import no.difi.oxalis.api.util.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin.Span;
import zipkin.reporter.Reporter;

/**
 * Implementation of ZipKin Reporter putting tracing data in SLF4J logger.
 *
 * @author erlend
 * @since 4.0.0
 */
@Singleton
@Type("slf4j")
public class Slf4jReporter implements Reporter<Span> {

    /**
     * Logger used for tracing data.
     */
    private final Logger logger = LoggerFactory.getLogger(Slf4jReporter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void report(Span span) {
        logger.info("{}", span);
    }
}
