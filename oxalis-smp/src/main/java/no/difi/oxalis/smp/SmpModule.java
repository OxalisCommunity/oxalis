package no.difi.oxalis.smp;

import com.google.inject.AbstractModule;
import eu.peppol.service.LookupService;
import no.difi.oxalis.smp.service.SmpLookupService;

import javax.inject.Singleton;

public class SmpModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(LookupService.class).to(SmpLookupService.class).in(Singleton.class);
    }
}
