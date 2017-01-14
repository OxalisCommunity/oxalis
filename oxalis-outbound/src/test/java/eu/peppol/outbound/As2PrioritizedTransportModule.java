package eu.peppol.outbound;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import no.difi.vefa.peppol.common.model.TransportProfile;

import java.util.Collections;
import java.util.List;

public class As2PrioritizedTransportModule extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    @Named("prioritized")
    List<TransportProfile> transportProfiles() {
        return Collections.singletonList(TransportProfile.AS2_1_0);
    }
}
