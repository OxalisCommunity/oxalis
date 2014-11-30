package eu.peppol.outbound.transmission;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.outbound.OxalisOutboundModule;
import eu.peppol.security.CommonName;
import eu.peppol.smp.*;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.StatisticsGranularity;
import eu.peppol.statistics.StatisticsTransformer;
import eu.peppol.util.GlobalConfiguration;
import org.busdox.smp.SignedServiceMetadataType;
import org.easymock.EasyMock;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

/**
 * Module which will provide the components needed for unit testing of the classes in
 * the eu.peppol.outbound.transmission package.
 *
 * The SmpLookupManager is especially important as it will provide a hard coded reference to our locally installed
 * AS2 end point for the PEPPOL Participant Identifier U4_TEST.
 *
 * @author steinar
 *         Date: 29.10.13
 *         Time: 11:42
 */
public class TransmissionTestModule extends AbstractModule {

    @Override
    protected void configure() {
        /* nothing */
    }

    @Provides
    MessageSenderFactory provideMessageSenderFactory() {
        return EasyMock.createMock(MessageSenderFactory.class);
    }

    @Provides
    @Named("OurCommonName")
    CommonName ourCommonName() {
        return new CommonName("APP_1000000006");
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

    @Provides @Singleton
    public SmpLookupManager getSmpLookupManager() {
        final SmpLookupManager realSmpLookupManager = new OxalisOutboundModule().getSmpLookupManager();
        return new SmpLookupManager() {
            @Override
            public URL getEndpointAddress(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {
                try {
                    if (participant.equals(WellKnownParticipant.U4_TEST))
                        return new URL("https://localhost:8080/oxalis/as2");
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

    @Provides
    GlobalConfiguration obtainConfiguration() {
        return GlobalConfiguration.getInstance();
    }

}
