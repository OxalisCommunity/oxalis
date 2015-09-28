/*
 * Copyright (c) 2015 Steinar Overbeck Cook
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.document;

import org.testng.annotations.Test;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.StringWriter;

/**
 * @author steinar
 *         Date: 27.07.15
 *         Time: 11.31
 */
public class SbdWrapperTest {

    /** Creates a message with a binary payload encoded with base64 */
    @Test
    public void createMesageWithBinaryPayload() throws Exception {

        StringWriter sw = new StringWriter();

        StandardBusinessDocument sbd = new StandardBusinessDocument();
        StandardBusinessDocumentHeader sbdh = new StandardBusinessDocumentHeader();
        sbd.setStandardBusinessDocumentHeader(sbdh);
        sbdh.setHeaderVersion("1.0");

        Partner senderPartner = new Partner();
        PartnerIdentification senderPartnerIdentification = new PartnerIdentification();
        senderPartnerIdentification.setAuthority("iso6523-actorid-upis");
        senderPartnerIdentification.setValue("9908:810017902");
        senderPartner.setIdentifier(senderPartnerIdentification);

        sbdh.getSender().add(senderPartner);

        Partner receiverPartner = new Partner();
        PartnerIdentification receiverPartnerIdentification = new PartnerIdentification();
        receiverPartnerIdentification.setAuthority("iso6523-actorid-upis");
        receiverPartnerIdentification.setValue("9908:810017902");

        sbdh.getReceiver().add(receiverPartner);

        DocumentIdentification documentIdentification = new DocumentIdentification();
        documentIdentification.setStandard("http://uri.etsi.org/02918/v1.2.1#");
        documentIdentification.setTypeVersion("1.0");
        documentIdentification.setInstanceIdentifier("FA4A6819-6149-4134-95C3-C53A65338EB6");
        documentIdentification.setType("asic");

        sbdh.setDocumentIdentification(documentIdentification);

        BusinessScope businessScope = new BusinessScope();
        Scope scope = new Scope();
        scope.setType("DOCUMENTID");
        scope.setInstanceIdentifier("urn:oasis:names:specification:ubl:schema:xsd:ExpressionOfInterest::ExpressionOfInterest##urn:www.cenbii.eu:transaction:biitrdm081:ver3.0::2.1");

        sbdh.setBusinessScope(businessScope);

        // Creates the binary element wrapped in XML element asic-zip-archive:
        //
        // <asic:asic-zip-archive xmlns:asic="urn:etsi.org:specification:02918:v.1.2.1">VGhlIGJpbmFyeSBjb250ZW50cw==</asic:asic-zip-archive>
        //
        QName qName = new QName("urn:etsi.org:specification:02918:v.1.2.1", "asic-zip-archive", "asic");
        JAXBElement<byte[]> anyObject = new JAXBElement<byte[]>(qName, byte[].class, "The binary contents".getBytes() );
        sbd.setAny(anyObject);

        // Marshalls everything from Java objects into XML text
        JAXBContext jaxbContext = JAXBContext.newInstance("org.unece.cefact.namespaces.standardbusinessdocumentheader", ObjectFactory.class.getClassLoader());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);

        // casts to jaxb root element using the utility methods from the ObjectFactory
        ObjectFactory of = new ObjectFactory();
        JAXBElement<StandardBusinessDocument> root = of.createStandardBusinessDocument(sbd);

        marshaller.marshal(root, sw);

        System.out.println(sw.toString());
    }
}
