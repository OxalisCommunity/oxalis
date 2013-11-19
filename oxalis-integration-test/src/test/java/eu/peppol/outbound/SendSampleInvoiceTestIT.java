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
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URL;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 18.11.13
 *         Time: 14:55
 */
public class SendSampleInvoiceTestIT {

    public static final String SAMPLE_DOCUMENT = "peppol-bis-invoice-sbdh.xml";

    @Test
    public void sendSingleInvoiceToLocalEndPoint() throws Exception {

        InputStream is = SendSampleInvoiceTestIT.class.getClassLoader().getResourceAsStream(SAMPLE_DOCUMENT);
        assertNotNull(is, "Unable to locate peppol-bis-invoice-sbdh.sml in class path");

        OxalisOutboundModule oxalisOutboundModule = new OxalisOutboundModule();

        TransmissionRequestBuilder builder = oxalisOutboundModule.getTransmissionRequestBuilder();
        builder.payLoad(is);
        builder.endPoint(new URL("https://localhost:8443/oxalis/as2"), BusDoxProtocol.AS2);
        TransmissionRequest transmissionRequest = builder.build();

        Transmitter transmitter = oxalisOutboundModule.getTransmitter();

        TransmissionResponse transmit = transmitter.transmit(transmissionRequest);

    }
}
