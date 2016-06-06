/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.outbound.transmission;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.outbound.IntegrationTestConstant;
import eu.peppol.outbound.OxalisOutboundModule;
import eu.peppol.security.*;
import eu.peppol.smp.ParticipantNotRegisteredException;
import eu.peppol.smp.SmpLookupException;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.smp.SmpSignedServiceMetaDataException;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.GlobalConfigurationImpl;
import org.busdox.servicemetadata.publishing._1.SignedServiceMetadataType;

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

    public static final String OUR_LOCAL_OXALIS_URL = IntegrationTestConstant.OXALIS_AS2_URL;

    @Override
    protected void configure() {
        bind(KeystoreLoader.class).to(PeppolKeystoreLoader.class).in(Singleton.class);
        bind(KeystoreManager.class).to(KeystoreManagerImpl.class).in(Singleton.class);
        bind(MessageSenderFactory.class);
    }

    @Provides
    GlobalConfiguration provideGlobalConfiguration() {
        return GlobalConfigurationImpl.getInstance();
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
    public AccessPointIdentifier provideAccessPointIdentifier(KeystoreManager keystoreManager) {
        return AccessPointIdentifier.valueOf(keystoreManager.getOurCommonName());
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

}
