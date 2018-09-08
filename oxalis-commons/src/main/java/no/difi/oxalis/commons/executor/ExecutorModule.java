package no.difi.oxalis.commons.executor;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.commons.guice.OxalisModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author erlend
 */
public class ExecutorModule extends OxalisModule {

    @Override
    protected void configure() {
        bindSettings(ExecutorConf.class);
    }

    @Provides
    @Singleton
    public ExecutorService getExecutorService(Settings<ExecutorConf> settings) {
        return Executors.newFixedThreadPool(settings.getInt(ExecutorConf.THREADS));
    }
}
