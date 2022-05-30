package network.oxalis.commons.executor;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.guice.OxalisModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author erlend
 * @since 4.0.3
 */
public class ExecutorModule extends OxalisModule {

    @Override
    protected void configure() {
        bindSettings(ExecutorConf.class);
    }

    @Provides
    @Singleton
    @Named("default")
    public ExecutorService getExecutorService(Settings<ExecutorConf> settings) {
        return Executors.newFixedThreadPool(settings.getInt(ExecutorConf.DEFAULT));
    }

    @Provides
    @Singleton
    @Named("statistics")
    public ExecutorService getStatisticsExecutorService(Settings<ExecutorConf> settings) {
        return Executors.newFixedThreadPool(settings.getInt(ExecutorConf.STATISTICS));
    }
}
