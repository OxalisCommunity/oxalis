/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
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

package eu.peppol.inbound.server;

import com.google.inject.Inject;
import com.google.inject.Injector;
import eu.peppol.inbound.guice.AccessPointServiceModule;
import eu.peppol.inbound.guice.MockWebServiceContextModule;
import eu.peppol.inbound.guice.WebServiceModule;
import eu.peppol.start.identifier.PeppolMessageHeader;
import eu.peppol.start.persistence.MessageRepository;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.w3._2009._02.ws_tra.Create;
import org.w3._2009._02.ws_tra.FaultMessage;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Verifies that the accessPointService works as expected, even when something goes wrong.
 *
 * @author steinar
 *         Date: 09.06.13
 *         Time: 21:42
 */
public class accessPointServiceTest {

    public static final String UNKNOWN_RECEIPIENT_MSG = "Unknown receipient";

    @Test
    public void messageRepoThrowsException() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, IOException {


        // Creates an object graph containing a MessageRepository, which throws an exception
        Injector injector = com.google.inject.Guice.createInjector(new AccessPointServiceModule(),
                new MockWebServiceContextModule(), new TestRepositoryModule(createMessageRepository()) );

        accessPointService ap = injector.getInstance(accessPointService.class);
        assertNotNull(ap);

        Create create = new Create();
        try {
            create.getAny().add(createDocument());
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Unable to create document");
        }

        Document document = ((Element) create.getAny().get(0)).getOwnerDocument();

        assertNotNull(document);

        try {
            ap.create(create);
            Assert.fail("The MessageRepository should have thrown an exception");
        } catch (FaultMessage faultMessage) {
            assertTrue(faultMessage.getMessage().contains(UNKNOWN_RECEIPIENT_MSG));
        }

    }

    MessageRepository createMessageRepository() {

        return new MessageRepository() {
            @Override
            public boolean saveInboundMessage(String inboundMessageStore, PeppolMessageHeader peppolMessageHeader, Document document) {
                throw new IllegalStateException(UNKNOWN_RECEIPIENT_MSG);
            }
        };
    }

    public Element createDocument() throws ParserConfigurationException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element root = document.createElement("soap-body");
        document.appendChild(root);

        Element invoice = document.createElement("invoice");
        root.appendChild(invoice);

        return invoice;
    }
}
