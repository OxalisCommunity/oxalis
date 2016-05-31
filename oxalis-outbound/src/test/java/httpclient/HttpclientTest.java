package httpclient;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.testng.annotations.Test;

import javax.net.ssl.SSLHandshakeException;
import java.net.ProxySelector;

import static org.testng.Assert.fail;

/**
 * Various tests to experiment with Apache HttpClient
 * Created by soc on 02.03.2016.
 */
@Test(groups = {"integration"})
public class HttpclientTest {


    @Test(expectedExceptions = {SSLHandshakeException.class})
    public void failWhenCertificateIsSelfSigned() throws Exception {
        HttpGet httpGet = new HttpGet("https://self-signed.badssl.com/");
        try (CloseableHttpClient httpClient = HttpClients.custom().build();
             CloseableHttpResponse response = httpClient.execute(httpGet);
        ) {
            fail("Attempt to http get from site with wrong host in SSL should have failed");
        }
    }

    @Test
    public void inspectCertificate() throws Exception {

        SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
        CloseableHttpClient httpClient = HttpClients.custom()
                .setRoutePlanner(routePlanner)
                .build();

        HttpGet httpGet = new HttpGet("https://badssl.com");

        HttpClientContext context = HttpClientContext.create();

        CloseableHttpResponse response = httpClient.execute(httpGet,context);

    }
}
