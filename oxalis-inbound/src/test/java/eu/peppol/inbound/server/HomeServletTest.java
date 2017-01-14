package eu.peppol.inbound.server;

import eu.peppol.inbound.AbstractJettyServerTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.HttpURLConnection;
import java.net.URL;

@Test(groups = "integration")
public class HomeServletTest extends AbstractJettyServerTest {

    @Test
    public void get() throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://localhost:8080/").openConnection();

        Assert.assertEquals(httpURLConnection.getResponseCode(), 200);
    }

    @Test
    public void post() throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://localhost:8080/").openConnection();
        httpURLConnection.setRequestMethod("POST");

        Assert.assertEquals(httpURLConnection.getResponseCode(), 500);
    }
}
