package eu.peppol.inbound.soap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Experimental SOAP Handler for the server side.
 * This simply serves as an example of implementing an inbound handler.
 *
 * @author steinar
 *         Date: 30.05.13
 *         Time: 18:37
 */
public class InboundSoapHandler implements SOAPHandler<SOAPMessageContext> {

    public static final Logger log = LoggerFactory.getLogger(InboundSoapHandler.class);

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }


    @Override
    public boolean handleMessage(SOAPMessageContext soapMessageContext) {
        Boolean outboundProperty = (Boolean) soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outboundProperty.booleanValue()) {
            log.debug("Outbound message being sent by METRO....");
        } else {
            log.debug("Inbound message being received....");
            try {
                SOAPMessage soapMessage = soapMessageContext.getMessage();
                SOAPHeader soapHeader = soapMessage.getSOAPHeader();
                Iterator<SOAPHeader> iter = soapHeader.examineAllHeaderElements();
                while (iter.hasNext()) {
                    SOAPHeaderElement soapHeaderElement = (SOAPHeaderElement) iter.next();
                    log.debug(soapHeaderElement.getNamespaceURI() + " " + soapHeaderElement.getLocalName());
                }

            } catch (SOAPException e) {
                // Never mind any errors, just report them
                log.warn(this.getClass().getSimpleName() + " " + e.getMessage(), e);
            } catch (Exception e) {
                log.warn("Error during inspection of SOAP message " + e, e);
            }
        }
        return true;
    }


    @Override
    public boolean handleFault(SOAPMessageContext soapMessageContext) {
        return true;
    }

    @Override
    public void close(MessageContext messageContext) {
    }
}
