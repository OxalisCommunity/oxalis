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

package eu.peppol.statistics;

import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolProcessTypeIdAcronym;
import eu.peppol.start.identifier.ChannelId;

/**
 * User: steinar
 * Date: 08.02.13
 * Time: 16:37
 */
public class RawStatisticsGenerator {

    public static RawStatistics sample() {
        RawStatistics rawStatistics = new RawStatistics.RawStatisticsBuilder().accessPointIdentifier(new AccessPointIdentifier("AP001"))
                .outbound()
            .sender(new ParticipantId("9908:810017902"))
            .receiver(new ParticipantId("9908:810017902"))
            .channel(new ChannelId("CH01"))
            .documentType(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier())
            .profile(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId())
            .build();
        return rawStatistics;
    }
}
