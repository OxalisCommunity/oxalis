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
package eu.peppol.inbound.soap.handler;

import eu.peppol.inbound.util.Log;
import eu.peppol.outbound.soap.SOAPHeaderObject;
import org.busdox.transport.identifiers._1.DocumentIdentifierType;
import org.busdox.transport.identifiers._1.ParticipantIdentifierType;
import org.busdox.transport.identifiers._1.ProcessIdentifierType;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Jose Gorvenia Narvaez(jose@alfa1lab.com)
 */
public class SOAPInboundHandler implements SOAPHandler<SOAPMessageContext> {

    public static final String MESSAGE_ID = "MessageIdentifier";
    public static final String CHANNEL_ID = "ChannelIdentifier";
    public static final String RECIPIENT_ID = "RecipientIdentifier";
    public static final String SENDER_ID = "SenderIdentifier";
    public static final String DOCUMENT_ID = "DocumentIdentifier";
    public static final String PROCESS_ID = "ProcessIdentifier";
    public static final String SCHEME = "scheme";

    private static String messageId;
    private static String channelId;
    private static ParticipantIdentifierType sender;
    private static ParticipantIdentifierType recipient;
    private static DocumentIdentifierType document;
    private static ProcessIdentifierType process;
    public static final SOAPHeaderObject soapHeader = new SOAPHeaderObject();

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        try {

            SOAPMessage message = context.getMessage();
            SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();

            Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

            if (!outbound) {
                SOAPHeader header = envelope.getHeader();
                @SuppressWarnings("unchecked")
                Iterator<SOAPHeaderElement> headerElements = header.examineAllHeaderElements();
                Log.info("InboundHeaders");

                while (headerElements.hasNext()) {
                    SOAPElement element = headerElements.next();
                    Log.info(element.getElementName().getLocalName() + ": " + element.getValue());
                    setHeaderElement(element);
                }

                SOAPInboundHandler.soapHeader.setMessageIdentifier(messageId);
                SOAPInboundHandler.soapHeader.setChannelIdentifier(channelId);
                SOAPInboundHandler.soapHeader.setSenderIdentifier(sender);
                SOAPInboundHandler.soapHeader.setRecipientIdentifier(recipient);
                SOAPInboundHandler.soapHeader.setDocumentIdentifier(document);
                SOAPInboundHandler.soapHeader.setProcessIdentifier(process);
            }
        } catch (SOAPException ex) {
            Log.error("Error retrieving the SOAP envelope", ex);
        }

        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void close(MessageContext mc) {
    }

    private void setHeaderElement(SOAPElement element) {

        if (element.getElementName().getLocalName().equalsIgnoreCase(MESSAGE_ID)) {
            messageId = element.getValue();
        }

        if (element.getElementName().getLocalName().equalsIgnoreCase(CHANNEL_ID)) {
            channelId = element.getValue();
        }

        if (element.getElementName().getLocalName().equalsIgnoreCase(RECIPIENT_ID)) {
            recipient = new ParticipantIdentifierType();
            recipient.setScheme(element.getAttribute(SCHEME));
            recipient.setValue(element.getValue());
        }

        if (element.getElementName().getLocalName().equalsIgnoreCase(SENDER_ID)) {
            sender = new ParticipantIdentifierType();
            sender.setScheme(element.getAttribute(SCHEME));
            sender.setValue(element.getValue());
        }

        if (element.getElementName().getLocalName().equalsIgnoreCase(DOCUMENT_ID)) {
            document = new DocumentIdentifierType();
            document.setScheme(element.getAttribute(SCHEME));
            document.setValue(element.getValue());
        }

        if (element.getElementName().getLocalName().equalsIgnoreCase(PROCESS_ID)) {
            process = new ProcessIdentifierType();
            process.setScheme(element.getAttribute(SCHEME));
            process.setValue(element.getValue());
        }
    }
}
