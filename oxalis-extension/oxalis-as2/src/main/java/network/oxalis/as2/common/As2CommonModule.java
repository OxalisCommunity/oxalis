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

package network.oxalis.as2.common;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.guice.OxalisModule;

/**
 * @author erlend
 * @since 4.0.2
 */
public class As2CommonModule extends OxalisModule {

    @Override
    protected void configure() {
        bindSettings(As2Conf.class);
    }

    @Provides
    @Singleton
    @Named("as2-notification")
    public String getNotification(Settings<As2Conf> settings) {
        return settings.getString(As2Conf.NOTIFICATION);
    }

}
