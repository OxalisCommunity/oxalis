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
package eu.peppol.inbound.soap;

import eu.peppol.inbound.util.Util;
import eu.peppol.outbound.soap.SoapHeader;
import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ObjectFactory;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 *
 * @author Jose Gorvenia Narvaez(jose@alfa1lab.com)
 */
public class SOAPHeaderDocument {

    public static final String NAMESPACE_TRANSPORT_IDS = "http://busdox.org/transport/identifiers/1.0/";
    public static final String QUALIFIED_NANE = "Headers";

    public static Document create(SoapHeader soapHeader){

        try {

            ObjectFactory objFactory = new ObjectFactory();
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.newDocument();

            Element top = document.createElementNS(NAMESPACE_TRANSPORT_IDS, QUALIFIED_NANE);

            document.appendChild(top);

            Marshaller marshaller = JAXBContext.newInstance(ParticipantIdentifierType.class).createMarshaller();
            marshaller.marshal(objFactory.createSenderIdentifier(soapHeader.getSenderIdentifier()), top);
            marshaller.marshal(objFactory.createRecipientIdentifier(soapHeader.getRecipientIdentifier()), top);

            marshaller = JAXBContext.newInstance(DocumentIdentifierType.class).createMarshaller();
            marshaller.marshal(objFactory.createDocumentIdentifier(soapHeader.getDocumentIdentifier()), top);

            marshaller = JAXBContext.newInstance(ProcessIdentifierType.class).createMarshaller();
            marshaller.marshal(objFactory.createProcessIdentifier(soapHeader.getProcessIdentifier()), top);

            return document;

        } catch(Exception e) {
            Util.logAndThrowRuntimeException("Problem creating SOAP header document", e);
        }

        return null;
    }
}
