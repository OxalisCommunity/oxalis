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
import com.google.inject.Provider;
import network.oxalis.api.settings.Settings;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author erlend
 * @since 4.0.4
 */
public class HostnameProvider implements Provider<String> {

    @Inject
    private Settings<IdentifierConf> settings;

    @Override
    public String get() {
        try {
            String hostname = settings.getString(IdentifierConf.HOSTNAME);
            if (hostname.trim().isEmpty())
                hostname = InetAddress.getLocalHost().getHostName();

            return hostname;
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to get local hostname.", e);
        }
    }
}
