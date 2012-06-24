/*
 * Version: MPL 1.1/EUPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Copyright The PEPPOL project (http://www.peppol.eu)
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL
 * (the "Licence"); You may not use this work except in compliance
 * with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * If you wish to allow use of your version of this file only
 * under the terms of the EUPL License and not to allow others to use
 * your version of this file under the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and
 * other provisions required by the EUPL License. If you do not delete
 * the provisions above, a recipient may use your version of this file
 * under either the MPL or the EUPL License.
 */
package eu.peppol.outbound.soap;

import eu.peppol.outbound.ssl.AccessPointX509TrustManager;
import eu.peppol.outbound.util.Log;
import eu.peppol.start.identifier.Configuration;
import eu.peppol.start.identifier.PeppolMessageHeader;
import org.w3._2009._02.ws_tra.AccessPointService;
import org.w3._2009._02.ws_tra.Create;
import org.w3._2009._02.ws_tra.FaultMessage;
import org.w3._2009._02.ws_tra.Resource;

import javax.net.ssl.*;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The accesspointClient class aims to hold all the processes required for consuming an AccessPoint.
 *
 * @author Dante Malaga(dante@alfa1lab.com)
 *         Jose Gorvenia Narvaez(jose@alfa1lab.com)
 */
public class SoapDispatcher {

    private static boolean initialised = false;

    public final void enableSoapLogging(boolean value) {
        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", String.valueOf(value));
    }

    /**
     * Sends a Create object using a given port and attaching the given SOAPHeaderObject data to the SOAP-envelope.
     *
     * @param endpointAddress the port which will be used to send the message.
     * @param messageHeader   the SOAPHeaderObject holding the BUSDOX headers
     *                        information that will be attached into the SOAP-envelope.
     * @param soapBody        Create object holding the SOAP-envelope payload.
     */
    public void send(URL endpointAddress, PeppolMessageHeader messageHeader, Create soapBody) {

        initialise();

        try {

            sendSoapMessage(endpointAddress, messageHeader, soapBody);

        } catch (FaultMessage e) {
            throw new RuntimeException("Failed to send SOAP message", e);
        }
    }

    private synchronized void initialise() {
        if (!initialised) {
            setDefaultHostnameVerifier();
            setDefaultSSLSocketFactory();
            initialised = true;
        }
    }

    /**
     * Gets and configures a port that points to a given webservice address.
     *
     * @param endpointAddress the address of the webservice.
     * @return the configured port.
     */
    private void sendSoapMessage(URL endpointAddress, final PeppolMessageHeader messageHeader, Create soapBody)
            throws FaultMessage {

        Log.debug("Constructing service proxy");

        AccessPointService accesspointService = new AccessPointService(
                getWsdlUrl(),
                new QName("http://www.w3.org/2009/02/ws-tra", "accessPointService"));

        accesspointService.setHandlerResolver(new HandlerResolver() {

            public List<Handler> getHandlerChain(PortInfo portInfo) {
                List<Handler> handlerList = new ArrayList<Handler>();
                handlerList.add(new SOAPOutboundHandler(messageHeader));
                return handlerList;
            }
        });

        Log.debug("Getting remote resource binding port");
        Resource port = null;
        try {
            port = accesspointService.getResourceBindingPort();
            Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress.toExternalForm());
            port.create(soapBody);

            Log.info("Sender:\t" + messageHeader.getSenderId().stringValue());
            Log.info("Recipient:\t" + messageHeader.getRecipientId().stringValue());
            Log.info("Destination:\t" + endpointAddress);
            Log.info("Message " + messageHeader.getMessageId() + " has been successfully delivered");

        } finally {
            // Creates memory leak if not performed
            if (port != null) {
                ((com.sun.xml.ws.Closeable) port).close();
            }
        }
    }

    private URL getWsdlUrl() {
        String wsdl = Configuration.getInstance().getWsdlFileName();
        String wsdlLocation = "META-INF/wsdl/" + wsdl + ".wsdl";
        URL wsdlUrl = getClass().getClassLoader().getResource(wsdlLocation);

        if (wsdlUrl == null) {
            throw new IllegalStateException("Unable to locate WSDL file " + wsdlLocation);
        }

        Log.debug("Found WSDL file at " + wsdlUrl);
        return wsdlUrl;
    }

    private void setDefaultSSLSocketFactory() {
        try {

            TrustManager[] trustManagers = new TrustManager[]{new AccessPointX509TrustManager(null, null)};
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        } catch (Exception e) {
            throw new RuntimeException("Error setting socket factory", e);
        }
    }

    private void setDefaultHostnameVerifier() {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            public boolean verify(final String hostname, final SSLSession session) {
                Log.debug("Void hostname verification OK");
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }
}
