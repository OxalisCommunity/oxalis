package eu.peppol.statistics;

import eu.peppol.start.identifier.*;

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
