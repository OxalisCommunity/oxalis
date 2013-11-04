package eu.peppol.outbound.transmission;

import com.google.inject.AbstractModule;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.smp.SmpLookupManagerImpl;

/**
 * @author steinar
 *         Date: 04.11.13
 *         Time: 10:06
 */
public class TransmissionModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(SmpLookupManager.class).to(SmpLookupManagerImpl.class);
    }
}
