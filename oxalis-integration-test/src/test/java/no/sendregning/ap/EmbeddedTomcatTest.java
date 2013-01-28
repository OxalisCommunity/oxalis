package no.sendregning.ap;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * User: steinar
 * Date: 16.12.12
 * Time: 22:04
 */
public class EmbeddedTomcatTest {

    @BeforeTest
    void setUp() throws ServletException, LifecycleException {

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.setBaseDir("/tmp");

        tomcat.addWebapp("/oxalis", "/Users/steinar/src/sr-peppol/aksesspunkt/oxalis/oxalis-start-inbound/target/oxalis");

        tomcat.start();
    }


    @Test
    public void testStartEmbeddedTomcat() throws LifecycleException, ServletException, IOException {

        testGetIndexHtml();

        System.out.println("Started!");

    }

    void testGetIndexHtml() throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://localhost:8080/oxalis/");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity entity = httpResponse.getEntity();
        entity.writeTo(System.out);

    }

}
