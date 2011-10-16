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
package eu.peppol.outbound.client;

import eu.peppol.outbound.soap.SOAPHeaderObject;
import eu.peppol.outbound.soap.handler.SOAPOutboundHandler;
import eu.peppol.outbound.util.Log;
import org.w3._2009._02.ws_tra.AccesspointService;
import org.w3._2009._02.ws_tra.Create;
import org.w3._2009._02.ws_tra.FaultMessage;
import org.w3._2009._02.ws_tra.Resource;

import javax.net.ssl.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
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
public class accesspointClient {

    public final void enableSoapLogging(boolean value) {
        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", String.valueOf(value));
    }

    private void setupCertificateTrustManager() {
        try {

            TrustManager[] trustManagers = new TrustManager[]{new AccessPointX509TrustManager(null, null)};
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustManagers, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        } catch (Exception e) {
            Log.error("Error setting the Certificate Trust Manager.", e);
        }
    }

    private void setupHostNameVerifier() {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            public boolean verify(final String hostname, final SSLSession session) {
                Log.info("HostName verification done");
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

    /**
     * Gets and configures a port that points to a given webservice address.
     *
     * @param address the address of the webservice.
     * @return the configured port.
     */
    private Resource setupEndpointAddress(final String address) {

        AccesspointService service = new AccesspointService();
        Map<String, Object> requestContext = null;
        Resource port = null;

        setupHostNameVerifier();
        setupCertificateTrustManager();

        service.setHandlerResolver(new HandlerResolver() {

            public List<Handler> getHandlerChain(final PortInfo pi) {
                List<Handler> handlerList = new ArrayList<Handler>();
                handlerList.add(new SOAPOutboundHandler());
                return handlerList;
            }
        });

        port = service.getResourceBindingPort();

        requestContext = ((BindingProvider) port).getRequestContext();
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);

        return port;
    }

    private Resource getPort(final String address) {
        Resource port = null;

        try {
            port = setupEndpointAddress(address);
        } catch (Exception e) {
            Log.error("Error setting the Endpoint Address.", e);
        }

        return port;
    }

    /**
     * Sends a Create object using a given port and attaching the given SOAPHeaderObject data to the SOAP-envelope.
     *
     * @param endpointAddress   the port which will be used to send the message.
     * @param soapHeader        the SOAPHeaderObject holding the BUSDOX headers
     *                          information that will be attached into the SOAP-envelope.
     * @param body              Create object holding the SOAP-envelope payload.
     */
    public final void send(String endpointAddress, SOAPHeaderObject soapHeader, Create body) {
        Resource port = getPort(endpointAddress);
        SOAPOutboundHandler.setSoapHeader(soapHeader);

        Log.info("Ready to send message"
                + "\n\tMessageID\t:"
                + soapHeader.getMessageIdentifier()
                + "\n\tChannelID\t:"
                + soapHeader.getChannelIdentifier()
                + "\n\tDocumentID\t:"
                + soapHeader.getDocumentIdentifier().getScheme() + ":"
                + soapHeader.getDocumentIdentifier().getValue()
                + "\n\tProcessID\t:"
                + soapHeader.getProcessIdentifier().getScheme() + ":"
                + soapHeader.getProcessIdentifier().getValue()
                + "\n\tSenderID\t:"
                + soapHeader.getSenderIdentifier().getScheme() + ":"
                + soapHeader.getSenderIdentifier().getValue()
                + "\n\tRecipientID\t:"
                + soapHeader.getRecipientIdentifier().getScheme() + ":"
                + soapHeader.getRecipientIdentifier().getValue());

        try {
            port.create(body);
            Log.info("Message " + soapHeader.getMessageIdentifier() + " has been successfully delivered!");
        } catch (FaultMessage e) {
            Log.error("Error while sending the message.", e);
        //} finally {
          // ((Closeable) port).close();
        }
    }
}
