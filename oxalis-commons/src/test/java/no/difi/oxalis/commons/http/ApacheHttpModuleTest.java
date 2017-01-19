package no.difi.oxalis.commons.http;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.IOException;

@Guice(modules = {ApacheHttpModule.class})
public class ApacheHttpModuleTest {

    @Inject
    private Provider<CloseableHttpClient> httpClientProvider;

    @Test
    public void simple() throws IOException {
        try (CloseableHttpClient httpClient1 = httpClientProvider.get();
             CloseableHttpClient httpClient2 = httpClientProvider.get()) {

            Assert.assertNotNull(httpClient1);
            Assert.assertNotNull(httpClient2);

            Assert.assertFalse(httpClient1 == httpClient2);
        }
    }
}
