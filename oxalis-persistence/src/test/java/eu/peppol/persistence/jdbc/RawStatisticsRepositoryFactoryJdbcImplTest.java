package eu.peppol.persistence.jdbc;

import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolProcessTypeIdAcronym;
import eu.peppol.persistence.guice.TestModuleFactory;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.RawStatisticsRepositoryFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

/**
 * @author steinar
 *         Date: 28.10.2016
 *         Time: 08.22
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class RawStatisticsRepositoryFactoryJdbcImplTest {

    @Inject
    RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory;

    @Test
    public void testGetInstanceForRawStatistics() throws Exception {

        RawStatisticsRepository repository = rawStatisticsRepositoryFactory.getInstanceForRawStatistics();

        RawStatistics rawStatistics = new RawStatistics.RawStatisticsBuilder()
                .accessPointIdentifier(new AccessPointIdentifier("AP_SendRegning"))
                .outbound()
                .sender(new ParticipantId("9908:810017902").toVefa())
                .receiver(new ParticipantId("9908:810017902").toVefa())
                .channel(new ChannelId("CH01"))
                .documentType(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier().toVefa())
                .profile(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId().toVefa())
                .build();
        repository.persist(rawStatistics);
    }

}