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

package no.difi.oxalis.commons.mode;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.api.CrlCache;
import no.difi.certvalidator.util.CrlUtils;
import no.difi.certvalidator.util.SimpleCachingCrlFetcher;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.URI;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;

/**
 * @author erlend
 */
@Singleton
public class OxalisCrlFetcher extends SimpleCachingCrlFetcher {

    @Inject
    private Provider<CloseableHttpClient> httpClientProvider;

    @Inject
    private Provider<RequestConfig> requestConfigProvider;

    @Inject
    public OxalisCrlFetcher(CrlCache crlCache) {
        super(crlCache);
    }

    @Override
    protected X509CRL httpDownload(String url) throws CertificateValidationException {
        try {
            HttpGet httpGet = new HttpGet(URI.create(url));
            httpGet.setConfig(requestConfigProvider.get());

            try (CloseableHttpResponse response = httpClientProvider.get().execute(httpGet)) {
                X509CRL crl = CrlUtils.load(response.getEntity().getContent());
                crlCache.set(url, crl);
                return crl;
            }
        } catch (IOException | CRLException e) {
            throw new CertificateValidationException(
                    String.format("Failed to download CRL '%s' (%s)", url, e.getMessage()), e);
        }
    }
}
