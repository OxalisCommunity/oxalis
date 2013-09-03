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
 * 
 * Updated by EDIGARD AS (Pavels Bubens). To specify timeouts for HTTP client. See http://metro.java.net/1.5/guide/HTTP_Timeouts.html
 */
package eu.peppol.outbound.soap;

import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.rx.rm.api.ReliableMessagingFeature;
import com.sun.xml.ws.rx.rm.api.ReliableMessagingFeatureBuilder;
import com.sun.xml.ws.rx.rm.api.RmProtocolVersion;
import eu.peppol.outbound.ssl.AccessPointX509TrustManager;
import eu.peppol.outbound.util.Log;
import eu.peppol.start.identifier.PeppolDocumentTypeId;
import eu.peppol.start.identifier.PeppolMessageHeader;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OxalisConstant;
import org.w3._2009._02.ws_tra.*;

import javax.net.ssl.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import java.net.URL;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.*;
import java.util.Map.Entry;

/**
 * The accesspointClient class aims to hold all the processes required for
 * consuming an AccessPoint.
 *
 * @author Dante Malaga(dante@alfa1lab.com) Jose Gorvenia
 *         Narvaez(jose@alfa1lab.com)
 *         <p/>
 *         <p/>
 *         Updated by ITP AS (Pavels Bubens). To specify timeouts for HTTP
 *         client. See http://metro.java.net/1.5/guide/HTTP_Timeouts.html
 * @author Steinar Overbeck Cook (steinar@sendregning.no)
 */
public class SoapDispatcher {

    private static boolean initialised = false;

    private static Integer connectTimeout = GlobalConfiguration.getInstance().getConnectTimeout();
    private static Integer readTimeout = GlobalConfiguration.getInstance().getReadTimeout();

    private static boolean add2ApBlackListOnTimeout = true;
    private static Integer apBlackListEntryKeepTime = 1000 * 60 * 120;

    private static Map<URL, Long> apBlackList = Collections.synchronizedMap(new LinkedHashMap<URL, Long>());

    public final void enableSoapLogging(boolean value) {
        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", String.valueOf(value));
    }

    // BlackList operations
    public static void setApBlackList(Map<URL, Long> apBlackList) {
        SoapDispatcher.apBlackList = apBlackList;
    }

    public static Map<URL, Long> getApBlackList() {
        return apBlackList;
    }

    public static Map<String, Date> getApBlackListAsString() {
        Map<String, Date> map = new LinkedHashMap<String, Date>();
        synchronized (SoapDispatcher.apBlackList) {
            for (Iterator<Entry<URL, Long>> iter = apBlackList.entrySet().iterator(); iter.hasNext(); ) {
                Entry<URL, Long> entry = iter.next();
                Date value = null;
                if (entry.getValue() != null && entry.getValue() > 0) {
                    value = new Date(entry.getValue());
                }
                map.put(entry.getKey().toExternalForm(), value);
            }
        }

        return map;
    }

    public static void setApBlackListFromString(Set<String> apBlackList)
            throws Exception {
        synchronized (SoapDispatcher.apBlackList) {
            Log.debug("Populating apBlackList from string");
            clearApBlackList();
            for (Iterator<String> iter = apBlackList.iterator(); iter.hasNext(); ) {
                String destination = iter.next();
                if (destination != null && destination.trim().length() > 0) {
                    add2ApBlackList(new URL(destination), 0);
                }
            }
        }
    }

    public static boolean isAdd2ApBlackListOnTimeout() {
        return add2ApBlackListOnTimeout;
    }

    public static void setAdd2ApBlackListOnTimeout(boolean add2ApBlackListOnTimeout) {
        Log.debug("setAdd2ApBlackListOnTimeout " + add2ApBlackListOnTimeout);
        SoapDispatcher.add2ApBlackListOnTimeout = add2ApBlackListOnTimeout;
    }

    public static Integer getApBlackListEntryKeepTime() {
        return apBlackListEntryKeepTime;
    }

    public static void setApBlackListEntryKeepTime(Integer apBlackListEntryKeepTime) {
        Log.debug("setApBlackListEntryKeepTime " + add2ApBlackListOnTimeout);
        SoapDispatcher.apBlackListEntryKeepTime = apBlackListEntryKeepTime;
    }

    public static void add2ApBlackList(URL destination, Integer apBlackListEntryKeepTime) {
        Log.debug("add2ApBlackList " + destination + " apBlackListEntryKeepTime " + apBlackListEntryKeepTime);
        if (apBlackListEntryKeepTime == null || apBlackListEntryKeepTime == 0) {
            getApBlackList().put(destination, new Long(0));
        } else {
            getApBlackList().put(destination, System.currentTimeMillis() + apBlackListEntryKeepTime);
        }
    }

    public static void removeFromApBlackList(URL destination) {
        Log.debug("removeFromApBlackList " + destination);
        getApBlackList().remove(destination);
    }

    public static boolean existInApBlackList(URL destination) {
        if (!getApBlackList().containsKey(destination)) {
            return false;
        }
        Long keepTime = getApBlackList().get(destination);
        if (keepTime == null || keepTime == 0) {
            return true;
        } else if (System.currentTimeMillis() < keepTime) {
            return true;
        }

        Log.debug("removeFromApBlackList due to getApBlackListEntryKeepTime");
        getApBlackList().remove(destination);
        return false;
    }

    public static void clearApBlackList() {
        Log.debug("clearApBlackList");
        getApBlackList().clear();
    }

    // Connection timeout operations
    public static Integer getConnectTimeout() {
        return connectTimeout;
    }

    public static void setConnectTimeout(Integer connectTimeout) {
        SoapDispatcher.connectTimeout = connectTimeout;
    }


    // Default read timeout
    public static Integer getReadTimeout() {
        return readTimeout;
    }

    public static void setReadTimeout(Integer readTimeout) {
        SoapDispatcher.readTimeout = readTimeout;
    }

    /**
     * copy-pasted from Apache ExceptionUtils
     */
    public static Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause != null) {
            throwable = cause;
            while ((throwable = throwable.getCause()) != null) {
                cause = throwable;
            }
        }
        return cause;
    }

    public static void setTimeouts(Map<String, Object> requestContext, PeppolMessageHeader messageHeader) {
        Integer readTimeout = getReadTimeout();
        Log.debug("setting connectTimeout " + getConnectTimeout() + " readTimeout " + readTimeout);
        requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, getConnectTimeout());
        requestContext.put("com.sun.xml.ws.request.timeout", readTimeout);
    }

    /**
     * Sends a Create object using a given port and attaching the given
     * SOAPHeaderObject data to the SOAP-envelope.
     *
     * @param endpointAddress the port which will be used to send the message.
     * @param messageHeader   the SOAPHeaderObject holding the BUSDOX headers information
     *                        that will be attached into the SOAP-envelope.
     * @param soapBody        Create object holding the SOAP-envelope payload.
     */
    public void send(URL endpointAddress, PeppolMessageHeader messageHeader, Create soapBody) throws FaultMessage {

        initialise();

        sendSoapMessage(endpointAddress, messageHeader, soapBody);
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

        if (endpointAddress == null) {
            throw new IllegalArgumentException("Recipient AP is null.");
        }

        if (existInApBlackList(endpointAddress)) {
            throw new RuntimeException("Recipient AP is not avalaible at the moment: "
                    + endpointAddress.toExternalForm()
                    + " . Please contact system administrator.");
        }

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
//            port = accesspointService.getResourceBindingPort();
            port = accesspointService.getResourceBindingPort(new ReliableMessagingFeatureBuilder(RmProtocolVersion.WSRM200702).closeSequenceOperationTimeout(1).build());

            Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();

            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress.toExternalForm());

            setTimeouts(requestContext, messageHeader);

            // Allows us to verify the name of the remote host.
//            requestContext.put(JAXWSProperties.HOSTNAME_VERIFIER, createHostnameVerifier());

            Log.info("Performing SOAP request to: " + endpointAddress.toExternalForm());
            port.create(soapBody);

            Log.info("Sender:\t" + messageHeader.getSenderId().stringValue());
            Log.info("Recipient:\t" + messageHeader.getRecipientId().stringValue());
            Log.info("Destination:\t" + endpointAddress);
            Log.info("Message " + messageHeader.getMessageId() + " has been successfully delivered");

        } catch (RuntimeException rte) {

            if (isAdd2ApBlackListOnTimeout() && getRootCause(rte) instanceof XMLStreamException) {
                Log.debug("Timeout exception occured. Will add to ApBlackList: " + endpointAddress);
                add2ApBlackList(endpointAddress, getApBlackListEntryKeepTime());
            }
            throw rte;
        } finally {
            // Creates memory leak if not performed
            if (port != null) {
                ((com.sun.xml.ws.Closeable) port).close();
            }
        }
    }

    private URL getWsdlUrl() {
        String wsdl = OxalisConstant.WSDL_FILE_NAME;
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
            sslContext.init(null, trustManagers, new SecureRandom()); // Uses default KeyManager but our own TrustManager
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        } catch (Exception e) {
            throw new RuntimeException("Error setting socket factory", e);
        }
    }

    /**
     * Establishes the hostname verifier to be used if and only if the name in the certificate and the hostname
     * don't match. Henceforth; you can not rely upon the hostname verifier to be invoked for each SSL session
     */
    private void setDefaultHostnameVerifier() {

        HostnameVerifier hostnameVerifier = createHostnameVerifier();

        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }


    private HostnameVerifier createHostnameVerifier() {
        return new OxalisHostnameVerifier();
    }


static class OxalisHostnameVerifier implements HostnameVerifier {

    public boolean verify(final String hostname, final SSLSession session) {
        try {
            Principal peerPrincipal = session.getPeerPrincipal();
            Log.warn("The remote host name '" + hostname + "' and SSL principal name '" + peerPrincipal.getName() + "' do not match!");
        } catch (SSLPeerUnverifiedException e) {
            Log.debug("Unable to retrieve SSL peer principal " + e);
        }
        Log.debug("Void hostname verification OK");
        return true;
    }

}
}