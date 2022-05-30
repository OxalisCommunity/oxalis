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

package network.oxalis.commons.config;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

/**
 * @author erlend
 * @since 4.0.0
 */
@Slf4j
public class JavaPropertiesPostConfig implements PostConfig {

    @Override
    public void perform(Config config) {
        if (!config.hasPath("oxalis.java"))
            return;

        config.getConfig("oxalis.java").entrySet().stream()
                .peek(e -> log.info("Property '{}' => '{}'", e.getKey(), String.valueOf(e.getValue().unwrapped())))
                .forEach(e -> System.setProperty(e.getKey(), String.valueOf(e.getValue().unwrapped())));
    }
}
