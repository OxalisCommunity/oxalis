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

package network.oxalis.commons.mode;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.opentelemetry.api.trace.Span;
import jakarta.inject.Named;
import network.oxalis.commons.certvalidator.api.CertificateValidationException;
import network.oxalis.commons.certvalidator.api.CrlCache;
import network.oxalis.commons.certvalidator.util.CrlUtils;
import network.oxalis.commons.certvalidator.util.SimpleCachingCrlFetcher;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.protocol.BasicHttpContext;

import java.io.IOException;
import java.net.URI;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;

/**
 * @author erlend
 * @since 4.0.0
 *
 * @author aaron-kumar
 * @since 5.0.0
 *
 */
@Singleton
public class OxalisCrlFetcher extends SimpleCachingCrlFetcher {

    @Inject
    private Provider<CloseableHttpClient> httpClientProvider;

    @Inject
    @Named("certificate")
    private RequestConfig requestConfig;

    @Inject
    public OxalisCrlFetcher(CrlCache crlCache) {
        super(crlCache);
    }

    @Override
    protected X509CRL httpDownload(String url) throws CertificateValidationException {
        try {
            Span span = Span.current();

            BasicHttpContext basicHttpContext = new BasicHttpContext();
            if (span != null)
                basicHttpContext.setAttribute(OxalisCrlFetcher.class.getName() + ".parentSpanContext", span.getSpanContext());

            HttpGet httpGet = new HttpGet(URI.create(url));
            httpGet.setConfig(requestConfig);

            try (CloseableHttpResponse response = httpClientProvider.get().execute(httpGet, basicHttpContext)) {
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
