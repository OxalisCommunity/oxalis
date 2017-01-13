package no.difi.oxalis.commons.evidence;

import com.google.inject.AbstractModule;
import no.difi.oxalis.api.evidence.EvidenceFactory;

import javax.inject.Singleton;

public class RemEvidenceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(EvidenceFactory.class).to(RemEvidenceFactory.class).in(Singleton.class);
    }
}
