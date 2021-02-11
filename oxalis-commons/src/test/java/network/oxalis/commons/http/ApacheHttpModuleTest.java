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

package network.oxalis.commons.http;

import com.google.inject.Inject;
import com.google.inject.Provider;
import network.oxalis.commons.guice.GuiceModuleLoader;
import org.apache.http.impl.client.CloseableHttpClient;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.IOException;

@Guice(modules = {GuiceModuleLoader.class})
public class ApacheHttpModuleTest {

    @Inject
    private Provider<CloseableHttpClient> httpClientProvider;

    @Test
    public void simple() throws IOException {
        try (CloseableHttpClient httpClient1 = httpClientProvider.get();
             CloseableHttpClient httpClient2 = httpClientProvider.get()) {

            Assert.assertNotNull(httpClient1);
            Assert.assertNotNull(httpClient2);

            Assert.assertNotSame(httpClient1, httpClient2);
        }
    }
}
