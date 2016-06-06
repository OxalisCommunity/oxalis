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
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.security.KeystoreLoader;
import eu.peppol.security.KeystoreManager;
import eu.peppol.security.KeystoreManagerImpl;
import eu.peppol.smp.*;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.StatisticsGranularity;
import eu.peppol.statistics.StatisticsTransformer;
import eu.peppol.util.DummyKeystoreLoader;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OperationalMode;
import eu.peppol.util.UnitTestGlobalConfigurationImpl;
import org.busdox.servicemetadata.publishing._1.SignedServiceMetadataType;
import org.easymock.EasyMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

/**
 * Module which will provide the components needed for unit testing of the classes in
 * the eu.peppol.outbound.transmission package.
 * <p>
 * The SmpLookupManager is especially important as it will provide a hard coded reference to our locally installed
 * AS2 end point for the PEPPOL Participant Identifier U4_TEST.
 *
 * @author steinar
 *         Date: 29.10.13
 *         Time: 11:42
 */
public class TransmissionTestModule extends AbstractModule {

    public static final Logger log = LoggerFactory.getLogger(TransmissionTestModule.class);

    @Override
    protected void configure() {

        bind(KeystoreLoader.class).to(DummyKeystoreLoader.class).in(Singleton.class);
        bind(KeystoreManager.class).to(KeystoreManagerImpl.class).in(Singleton.class);
    }


    @Provides
    @Singleton
    GlobalConfiguration provideTestConfiguration() {
        GlobalConfiguration globalConfiguration = UnitTestGlobalConfigurationImpl.createInstance();
        log.debug("Creating new configuration: " + globalConfiguration + " this should only happen once due to the @Singleton annotation");

        return globalConfiguration;
    }

    @Provides
    MessageSenderFactory provideMessageSenderFactory() {
        return EasyMock.createMock(MessageSenderFactory.class);
    }

    @Provides
    RawStatisticsRepository obtainRawStaticsRepository() {
        // Fake RawStatisticsRepository
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

    @Provides
    @Singleton
    public SmpLookupManager getSmpLookupManager() {
        final SmpLookupManager realSmpLookupManager = new SmpLookupManagerImpl(new SmpContentRetrieverImpl(),
                new DefaultBusDoxProtocolSelectionStrategyImpl(), OperationalMode.TEST, null);
        return new SmpLookupManager() {
            @Override
            public URL getEndpointAddress(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {
                try {
                    if (!isSmpLookupRequiredForParticipant(participant))
                        return new URL("https://localhost:8080/oxalis/as2");
                    else
                        return realSmpLookupManager.getEndpointAddress(participant, documentTypeIdentifier);
                } catch (MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
            }

            protected boolean isSmpLookupRequiredForParticipant(ParticipantId participant) {
                if (
                        participant.equals(WellKnownParticipant.U4_TEST)
                                || participant.equals(WellKnownParticipant.DIFI_TEST)
                                || participant.equals(new ParticipantId("9954:111111111"))
                    )
                    return false;
                else
                    return true;
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
                    if (!isSmpLookupRequiredForParticipant(participantId))
                        return new PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.AS2);
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
