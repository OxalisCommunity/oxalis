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

import com.google.inject.Injector;
import eu.peppol.PeppolMessageMetaData;
import eu.peppol.inbound.guice.AccessPointServiceModule;
import eu.peppol.inbound.guice.MockWebServiceContextModule;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.OxalisMessagePersistenceException;
import eu.peppol.smp.SmpModule;
import eu.peppol.statistics.*;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import org.w3._2009._02.ws_tra.Create;
import org.w3._2009._02.ws_tra.FaultMessage;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Date;

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
@Test(groups = "integration")
public class accessPointServiceTest {

    public static final String UNKNOWN_RECEIPIENT_MSG = "Unknown recipient";


    /**
     * Verifies that a fault message is thrown if persistence of the message fails.
     *
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws NoSuchProviderException
     * @throws IOException
     */
    @Test
    public void messageRepoThrowsException() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, IOException {


        // Creates an object graph containing a MessageRepository, which throws an exception
        Injector injector = com.google.inject.Guice.createInjector(
                new AccessPointServiceModule(),
                new MockWebServiceContextModule(),
                new SmpModule(),
                new TestRepositoryModule(createFailingMessageRepository(), createNormalStatisticsRepository())
        );

        accessPointService ap = injector.getInstance(accessPointService.class);
        assertNotNull(ap);

        Create create = createSampleSoapData();

        try {
            ap.create(create);
            fail("The MessageRepository should have thrown an exception");
        } catch (FaultMessage faultMessage) {
            assertNotNull(faultMessage.getMessage());
            assertTrue(faultMessage.getMessage().contains("Unable to persist"));
        }
    }


    @Test
    public void statisticsRepoThrowsException() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, IOException {
        // Creates an object graph containing a MessageRepository, which throws an exception
        Injector injector = com.google.inject.Guice.createInjector(
                new AccessPointServiceModule(),
                new MockWebServiceContextModule(),
                new SmpModule(),
                new TestRepositoryModule(createNormalMessageRepository(), createFailingStatisticsRepository())
        );

        accessPointService ap = injector.getInstance(accessPointService.class);
        assertNotNull(ap);

        Create create = createSampleSoapData();

        try {
            ap.create(create);
        } catch (FaultMessage faultMessage) {
            fail("No exception should be thrown if persistence of statistics fails: " + faultMessage);
        }

    }


    private Create createSampleSoapData() {
        // Creates the SOAP Create method to be supplied to the Access Point service
        Create create = new Create();
        try {
            create.getAny().add(createDocument());
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Unable to create document");
        }

        Document document = ((Element) create.getAny().get(0)).getOwnerDocument();
        assertNotNull(document);
        return create;
    }


    RawStatisticsRepository createNormalStatisticsRepository() {
        return new RawStatisticsRepository() {


            @Override
            public Integer persist(RawStatistics rawStatistics) {
                return null;
            }

            @Override
            public void fetchAndTransformRawStatistics(StatisticsTransformer transformer, Date start, Date end, StatisticsGranularity granularity) {
            }
        };

    }

    RawStatisticsRepository createFailingStatisticsRepository() {
        return new RawStatisticsRepository() {


            @Override
            public Integer persist(RawStatistics rawStatistics) {
                throw new IllegalStateException("Persistence of statistics failed");
            }

            @Override
            public void fetchAndTransformRawStatistics(StatisticsTransformer transformer, Date start, Date end, StatisticsGranularity granularity) {
            }
        };

    }

    MessageRepository createFailingMessageRepository() {

        return new MessageRepository() {
            @Override
            public void saveInboundMessage(PeppolMessageMetaData peppolMessageHeader, Document document) throws OxalisMessagePersistenceException {
                throw new OxalisMessagePersistenceException(UNKNOWN_RECEIPIENT_MSG, peppolMessageHeader);
            }

            @Override
            public void saveInboundMessage(PeppolMessageMetaData peppolMessageMetaData, InputStream payloadInputStream) throws OxalisMessagePersistenceException {

            }
        };
    }

    MessageRepository createNormalMessageRepository() {
        return new MessageRepository() {
            @Override
            public void saveInboundMessage(PeppolMessageMetaData peppolMessageHeader, Document document) throws OxalisMessagePersistenceException {

            }

            @Override
            public void saveInboundMessage(PeppolMessageMetaData peppolMessageMetaData, InputStream payloadInputStream) throws OxalisMessagePersistenceException {

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
