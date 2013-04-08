package eu.peppol.statistics;

import eu.peppol.start.identifier.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author steinar
 *         Date: 05.04.13
 *         Time: 16:20
 */
class AggregatedStatisticsSampleGenerator {


    private final List<AccessPointMetaData> accessPointMetaDataList;

    AggregatedStatisticsSampleGenerator() {
        accessPointMetaDataList = TestUtil.loadSampleAccessPointMetaData().getAccessPointMetaDataList();
    }

    public Collection<AggregatedStatistics> generateEntries(int count) {


        List<AggregatedStatistics> result = new ArrayList<AggregatedStatistics>();
        int countOfAccesspoints = accessPointMetaDataList.size();
        AccessPointIdentifier accessPointIdentifier = accessPointMetaDataList.get(randomNumberInRange(0, countOfAccesspoints)).getAccessPointIdentifier();
        for (int i = 0; i < count; i++) {


            AggregatedStatistics.Builder builder = new AggregatedStatistics.Builder();
            AggregatedStatistics aggregatedStatistics = builder
                    .accessPointIdentifier(accessPointIdentifier)
                    .count(randomNumberInRange(1, 10000))
                    .date(new Date())
                    .participantId(new ParticipantId("9908:" + 123456789))
                    .channel(new ChannelId("CH1"))
                    .direction(Direction.values()[randomNumberInRange(0, 1)])
                    .documentType(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier())
                    .profile(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId())
                    .build();

            result.add(aggregatedStatistics);

        }
        return result;
    }

    int randomNumberInRange(int min, int max) {

        int randomInteger = min + (int) (Math.random() * ((max - min) + 1));
        return randomInteger;
    }
}
