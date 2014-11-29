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

package eu.peppol.outbound;

import eu.peppol.BusDoxProtocol;
import eu.peppol.outbound.transmission.TransmissionRequest;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import eu.peppol.outbound.transmission.TransmissionResponse;
import eu.peppol.outbound.transmission.Transmitter;
import eu.peppol.security.CommonName;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 18.11.13
 *         Time: 14:55
 */
public class SendSampleInvoiceTestIT {

    public static final String SAMPLE_DOCUMENT = "peppol-bis-invoice-sbdh.xml";
    public static final String EHF_NO_SBDH = "BII04_T10_EHF-v1.5_invoice.xml";

    @BeforeClass
    public void setUp() {

    }

    @Test
    public void sendSingleInvoiceToLocalEndPointUsingAS2() throws Exception {

        InputStream is = SendSampleInvoiceTestIT.class.getClassLoader().getResourceAsStream(SAMPLE_DOCUMENT);
        assertNotNull(is, "Unable to locate peppol-bis-invoice-sbdh.sml in class path");

        // Creates and wires up an Oxalis outbound module (Guice)
        OxalisOutboundModule oxalisOutboundModule = new OxalisOutboundModule();

        // Creates a builder, which will build our transmission request
        TransmissionRequestBuilder builder = oxalisOutboundModule.getTransmissionRequestBuilder();
        builder.payLoad(is);

        // Overrides the end point address, thus preventing a SMP lookup
        builder.overrideAs2Endpoint(new URL("https://localhost:8443/oxalis/as2"), "peppol-APP_1000000006");

        // Builds our transmission request
        TransmissionRequest transmissionRequest = builder.build();

        // Gets a transmitter, which will be used to execute our transmission request
        Transmitter transmitter = oxalisOutboundModule.getTransmitter();

        // Transmits our transmission request
        TransmissionResponse transmissionResponse = transmitter.transmit(transmissionRequest);
        assertNotNull(transmissionResponse);
        assertNotNull(transmissionResponse.getTransmissionId());
        assertNotNull(transmissionResponse.getStandardBusinessHeader());
        assertEquals(transmissionResponse.getStandardBusinessHeader().getRecipientId().stringValue(), "9908:810017902");
        assertEquals(transmissionResponse.getURL().toExternalForm(), "https://localhost:8443/oxalis/as2");
        assertEquals(transmissionResponse.getProtocol(), BusDoxProtocol.AS2);
        assertEquals(transmissionResponse.getCommonName().toString(), "peppol-APP_1000000006");

    }


    /**
     * This will not work if you have set up your oxalis-persistence extension to use
     * a JNDI data source.
     *
     * This could be fixed by changing the oxalis-global.properties to not use a custom persistence
     * module for incoming messages. Needs to be fixed sooner or later. -- Steinar, Dec 1, 2013
     *
     * @throws MalformedURLException
     */
    @Test()
    public void sendSingleInvoiceToLocalEndPointUsingSTART() throws MalformedURLException {
        InputStream is = SendSampleInvoiceTestIT.class.getClassLoader().getResourceAsStream(EHF_NO_SBDH);
        assertNotNull(is, EHF_NO_SBDH + " not found in the class path");

        OxalisOutboundModule oxalisOutboundModule = new OxalisOutboundModule();
        assertNotNull(oxalisOutboundModule);

        TransmissionRequestBuilder builder = oxalisOutboundModule.getTransmissionRequestBuilder();
        builder.payLoad(is);
        builder.overrideEndpointForStartProtocol(new URL("https://localhost:8443/oxalis/accessPointService"));
        TransmissionRequest transmissionRequest = builder.build();
        assertNotNull(transmissionRequest);

        Transmitter transmitter = oxalisOutboundModule.getTransmitter();

        // Transmits our transmission request
        TransmissionResponse transmissionResponse = transmitter.transmit(transmissionRequest);
        assertNotNull(transmissionResponse);
        assertNotNull(transmissionResponse.getTransmissionId());
        assertNotNull(transmissionResponse.getStandardBusinessHeader());
        assertEquals(transmissionResponse.getStandardBusinessHeader().getRecipientId().stringValue(), "0088:1234567987654");
        assertEquals(transmissionResponse.getURL().toExternalForm(), "https://localhost:8443/oxalis/accessPointService");
        assertEquals(transmissionResponse.getProtocol(), BusDoxProtocol.START);
        assertEquals(transmissionResponse.getCommonName(), new CommonName("")); // not used for START

    }

    // TODO: implement integration test for retrieval of the WSDL

    // TODO: implement integration test for retrieval of statistics
}

