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

package no.difi.oxalis.commons.config;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author erlend
 * @since 4.0.0
 */
public class JavaPropertiesPostConfig implements PostConfig {

    private static Logger logger = LoggerFactory.getLogger(JavaPropertiesPostConfig.class);

    @Override
    public void perform(Config config) {
        if (!config.hasPath("oxalis.java"))
            return;

        config.getConfig("oxalis.java").entrySet().stream()
                .peek(e -> logger.info("Property '{}' => '{}'", e.getKey(), String.valueOf(e.getValue().unwrapped())))
                .forEach(e -> System.setProperty(e.getKey(), String.valueOf(e.getValue().unwrapped())));
    }
}
