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

package eu.peppol.util;

import com.google.inject.Singleton;
import no.difi.oxalis.api.config.GlobalConfiguration;
import no.difi.oxalis.api.config.PropertyDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a fake GlobalConfiguration instance, which works with our unit tests requiring access to an environment
 * in which a certificate is available.
 *
 * @author soc
 */
@Singleton
public class UnitTestGlobalConfigurationImpl implements GlobalConfiguration {

    public static final Logger log = LoggerFactory.getLogger(UnitTestGlobalConfigurationImpl.class);

    // In testing the default is to allow overrides
    private Boolean transmissionBuilderOverride = true;


    private UnitTestGlobalConfigurationImpl() {
        // No action.
    }

    public static GlobalConfiguration createInstance() {
        return new UnitTestGlobalConfigurationImpl();
    }

    @Override
    public String getInboundLoggingConfiguration() {
        return null;
    }

    @Override
    public Boolean isTransmissionBuilderOverride() {
        return transmissionBuilderOverride;
    }

    @Override
    public void setTransmissionBuilderOverride(Boolean transmissionBuilderOverride) {
        if (this.transmissionBuilderOverride != transmissionBuilderOverride) {
            log.warn("Property " + PropertyDef.TRANSMISSION_BUILDER_OVERRIDE.getPropertyName() + " is being changed to " + transmissionBuilderOverride + " from " + this.transmissionBuilderOverride);
        }

        this.transmissionBuilderOverride = transmissionBuilderOverride;
    }
}
