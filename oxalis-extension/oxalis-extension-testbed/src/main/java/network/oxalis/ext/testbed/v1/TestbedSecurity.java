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

package network.oxalis.ext.testbed.v1;

import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import network.oxalis.api.settings.Settings;

import java.nio.charset.StandardCharsets;

/**
 * @author erlend
 */
@Singleton
public class TestbedSecurity {

    private String expectedAuthorization;

    @Inject
    public void init(Settings<TestbedConf> settings) {
        expectedAuthorization = Hashing.sha256()
                .hashString(settings.getString(TestbedConf.PASSWORD), StandardCharsets.UTF_8)
                .toString();

        System.out.println(expectedAuthorization);
    }

    public String getExpectedAuthorization() {
        return expectedAuthorization;
    }
}
