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
import io.opentracing.contrib.apache.http.client.Constants;
import io.opentracing.contrib.spanmanager.DefaultSpanManager;
import io.opentracing.contrib.spanmanager.SpanManager;
import network.oxalis.pkix.ocsp.api.OcspFetcher;
import network.oxalis.pkix.ocsp.api.OcspFetcherResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author erlend
 * @since 4.0.0
 *
 * @author aaron-kumar
 * @since 5.0.0
 */
@Singleton
public class OxalisOcspFetcher implements OcspFetcher {

    @Inject
    private Provider<CloseableHttpClient> httpClientProvider;

    @Inject
    @Named("certificate")
    private RequestConfig requestConfig;

    @Override
    public OcspFetcherResponse fetch(URI uri, byte[] content) throws IOException {
        SpanManager.ManagedSpan span = DefaultSpanManager.getInstance().current();

        BasicHttpContext basicHttpContext = new BasicHttpContext();
        if (span.getSpan() != null)
            basicHttpContext.setAttribute(Constants.PARENT_CONTEXT, span.getSpan().context());

        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Content-Type", "application/ocsp-request");
        httpPost.setHeader("Accept", "application/ocsp-response");
        httpPost.setEntity(new ByteArrayEntity(content));
        httpPost.setConfig(requestConfig);

        return new ApacheOcspFetcherResponse(httpClientProvider.get().execute(httpPost, basicHttpContext));
    }

    private class ApacheOcspFetcherResponse implements OcspFetcherResponse {

        private CloseableHttpResponse response;

        public ApacheOcspFetcherResponse(CloseableHttpResponse response) {
            this.response = response;
        }

        @Override
        public int getStatus() {
            return response.getStatusLine().getStatusCode();
        }

        @Override
        public String getContentType() {
            return response.getFirstHeader("Content-Type").getValue();
        }

        @Override
        public InputStream getContent() throws IOException {
            return response.getEntity().getContent();
        }

        @Override
        public void close() throws IOException {
            response.close();
            response = null;
        }
    }
}
