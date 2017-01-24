package no.difi.oxalis.commons.statistics;

import com.google.inject.*;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import no.difi.oxalis.api.statistics.StatisticsService;

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
    StatisticsService getStatisticsService(Injector injector, Config config) {
        return injector.getInstance(
                Key.get(StatisticsService.class, Names.named(config.getString("statistics.service"))));
    }
}
