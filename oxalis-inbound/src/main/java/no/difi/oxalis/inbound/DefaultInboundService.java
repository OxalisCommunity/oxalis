package no.difi.oxalis.inbound;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.inbound.InboundService;
import no.difi.oxalis.api.statistics.StatisticsService;

/**
 * @author erlend
 * @since 4.0.2
 */
@Singleton
public class DefaultInboundService implements InboundService {

    private StatisticsService statisticsService;

    @Inject
    public DefaultInboundService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Override
    public void complete(InboundMetadata inboundMetadata) {
        statisticsService.persist(inboundMetadata);
    }
}
