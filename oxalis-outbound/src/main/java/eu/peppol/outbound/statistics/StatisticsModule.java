package eu.peppol.outbound.statistics;

import com.google.inject.*;
import com.google.inject.name.Names;
import no.difi.oxalis.api.statistics.StatisticsService;
import no.difi.vefa.peppol.mode.Mode;

public class StatisticsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(StatisticsService.class, Names.named("default")))
                .to(DefaultStatisticsService.class)
                .in(Singleton.class);

        bind(Key.get(StatisticsService.class, Names.named("noop")))
                .to(NoopStatisticsService.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    StatisticsService getStatisticsService(Mode mode, Injector injector) {
        return injector.getInstance(
                Key.get(StatisticsService.class, Names.named(mode.getString("statistics.service"))));
    }
}
