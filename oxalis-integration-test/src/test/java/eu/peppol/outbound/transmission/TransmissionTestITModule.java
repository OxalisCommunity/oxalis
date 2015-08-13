/*
 * Copyright (c) 2011,2012,2013,2014 UNIT4 Agresso AS.
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

package eu.peppol.outbound.transmission;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.outbound.OxalisOutboundModule;
import eu.peppol.security.CommonName;
import eu.peppol.smp.*;
import eu.peppol.util.GlobalConfiguration;
import org.busdox.smp.SignedServiceMetadataType;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 * @author thore
 */
public class TransmissionTestITModule extends AbstractModule {

    public static final String OUR_LOCAL_OXALIS_URL = "https://localhost:8443/oxalis/as2";

    @Override
    protected void configure() {
        bind(MessageSenderFactory.class);
    }

    @Provides
    @Named("sample-xml-with-sbdh")
    public InputStream getSampleXmlInputStream() {
        InputStream resourceAsStream = TransmissionTestITModule.class.getClassLoader().getResourceAsStream("peppol-bis-invoice-sbdh.xml");
        assertNotNull(resourceAsStream, "Unable to load " + "peppol-bis-invoice-sbdh.xml" + " from class path");
        return resourceAsStream;
    }

    @Provides
    @Named("invoice-to-itsligo")
    public InputStream sampleInvoiceWithSbdhForItSligo() {
        InputStream resourceAsStream = TransmissionTestITModule.class.getClassLoader().getResourceAsStream("peppol-bis-invoice-sbdh-itsligo.xml");
        assertNotNull(resourceAsStream, "Unable to load " + "peppol-bis-invoice-sbdh-itsligo.xml" + " from class path");
        return resourceAsStream;
    }

    @Provides
    public SmpLookupManager getSmpLookupManager() {

        final SmpLookupManager realSmpLookupManager = new OxalisOutboundModule().getSmpLookupManager();
        return new SmpLookupManager() {

            @Override
            public URL getEndpointAddress(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {
                try {

                    if (participant.equals(WellKnownParticipant.U4_TEST))
                        return new URL(OUR_LOCAL_OXALIS_URL);
                    else
                        return realSmpLookupManager.getEndpointAddress(participant, documentTypeIdentifier);
                } catch (MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public X509Certificate getEndpointCertificate(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {
                throw new IllegalStateException("not supported yet");
            }

            @Override
            public List<PeppolDocumentTypeId> getServiceGroups(ParticipantId participantId) throws SmpLookupException, ParticipantNotRegisteredException {
                throw new IllegalStateException("Not supported yet.");
            }

            @Override
            public PeppolEndpointData getEndpointTransmissionData(ParticipantId participantId, PeppolDocumentTypeId documentTypeIdentifier) {
                try {
                    if (participantId.equals(WellKnownParticipant.U4_TEST))
                        return new PeppolEndpointData(new URL(OUR_LOCAL_OXALIS_URL), BusDoxProtocol.AS2, new CommonName("APP_1000000006"));
                    else
                        return realSmpLookupManager.getEndpointTransmissionData(participantId, documentTypeIdentifier);
                } catch (MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public SignedServiceMetadataType getServiceMetaData(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) throws SmpSignedServiceMetaDataException {
                return null;
            }
        };
    }

    @Provides
    GlobalConfiguration obtainConfiguration() {
        return GlobalConfiguration.getInstance();
    }

}
