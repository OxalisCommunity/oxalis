package eu.peppol.inbound.server;

import eu.peppol.inbound.AbstractJettyServerTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.HttpURLConnection;
import java.net.URL;

public class StatusServletTest extends AbstractJettyServerTest {

    @Test
    public void get() throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://localhost:8080/status").openConnection();

        Assert.assertEquals(httpURLConnection.getResponseCode(), 200);
    }

    @Test
    public void post() throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://localhost:8080/status").openConnection();
        httpURLConnection.setRequestMethod("POST");

        Assert.assertEquals(httpURLConnection.getResponseCode(), 500);
    }
}
