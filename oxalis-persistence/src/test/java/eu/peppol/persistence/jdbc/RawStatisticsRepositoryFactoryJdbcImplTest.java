/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.peppol.persistence.jdbc;

import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolProcessTypeIdAcronym;
import eu.peppol.persistence.guice.TestModuleFactory;
import no.difi.oxalis.api.statistics.ChannelId;
import eu.peppol.statistics.DefaultRawStatistics;
import no.difi.oxalis.api.statistics.RawStatisticsRepository;
import no.difi.oxalis.api.statistics.RawStatisticsRepositoryFactory;
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

        DefaultRawStatistics rawStatistics = new DefaultRawStatistics.RawStatisticsBuilder()
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
