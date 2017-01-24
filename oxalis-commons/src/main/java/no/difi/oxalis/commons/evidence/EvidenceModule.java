package no.difi.oxalis.commons.evidence;

import com.google.inject.*;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import no.difi.oxalis.api.evidence.EvidenceFactory;

/**
 * @author erlend
 * @since 4.0.0
 */
public class EvidenceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(EvidenceFactory.class, Names.named("default")))
                .to(DefaultEvidenceFactory.class)
                .in(Singleton.class);

        bind(Key.get(EvidenceFactory.class, Names.named("rem")))
                .to(RemEvidenceFactory.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    protected EvidenceFactory getEvidenceFactory(Injector injector, Config config) {
        return injector.getInstance(Key.get(EvidenceFactory.class, Names.named(config.getString("evidence.service"))));
    }
}
