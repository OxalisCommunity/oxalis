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

package network.oxalis.outbound.lookup;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import network.oxalis.vefa.peppol.lookup.fetcher.BasicApacheFetcher;
import network.oxalis.vefa.peppol.mode.Mode;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * @author erlend
 * @since 4.0.0
 */
@Singleton
class OxalisApacheFetcher extends BasicApacheFetcher {

    private Provider<CloseableHttpClient> httpClientProvider;

    @Inject
    public OxalisApacheFetcher(Provider<CloseableHttpClient> httpClientProvider, Mode mode) {
        super(mode);
        this.httpClientProvider = httpClientProvider;
    }

    @Override
    protected CloseableHttpClient createClient() {
        return httpClientProvider.get();
    }
}
