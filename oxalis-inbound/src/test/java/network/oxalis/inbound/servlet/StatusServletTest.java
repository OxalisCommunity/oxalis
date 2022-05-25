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

package network.oxalis.inbound.servlet;

import com.google.inject.Injector;
import network.oxalis.inbound.OxalisGuiceContextListener;
import network.oxalis.test.jetty.AbstractJettyServerTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.net.URL;

@Test
public class StatusServletTest extends AbstractJettyServerTest {

    @Override
    public Injector getInjector() {
        return new OxalisGuiceContextListener().getInjector();
    }

    @Test
    public void get() throws Exception {
        HttpURLConnection httpURLConnection =
                (HttpURLConnection) new URL("http://localhost:8080/status").openConnection();

        Assert.assertEquals(httpURLConnection.getResponseCode(), HttpServletResponse.SC_OK);
    }

    @Test
    public void post() throws Exception {
        HttpURLConnection httpURLConnection =
                (HttpURLConnection) new URL("http://localhost:8080/status").openConnection();
        httpURLConnection.setRequestMethod("POST");

        Assert.assertEquals(httpURLConnection.getResponseCode(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
