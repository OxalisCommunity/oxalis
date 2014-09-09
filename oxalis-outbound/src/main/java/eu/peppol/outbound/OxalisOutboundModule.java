package eu.peppol.outbound;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import eu.peppol.outbound.transmission.TransmissionModule;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import eu.peppol.outbound.transmission.Transmitter;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.smp.SmpModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Object factory for the Oxalis outbound module.
 * Serves as the main entry point for external applications etc.
 *
 * TODO: Configure outbound logging
 *
 * @author steinar
 * @author thore
 */
public class OxalisOutboundModule {

    private Injector injector;

    /**
     * Constructor to boot guice for scratch without other Modules
     */
    public OxalisOutboundModule() {
        this(new Module[0]);
    }

    /**
     * Constructor to boot Guice with other external Modules (eases use from other Guice applications)
     * @param modules a list of zero or more Module instances to be registered with the injector
     */
    public OxalisOutboundModule(Module... modules) {
        List<Module> mods = new ArrayList<Module>();
        mods.addAll(Arrays.asList(modules));
        mods.add(new SmpModule());
        mods.add(new TransmissionModule());
        injector = Guice.createInjector(mods);
    }

    /**
     * Retrieves instances of TransmissionRequestBuilder, while not exposing Google Guice to the outside
     * @return instance of TransmissionRequestBuilder
     */
    public TransmissionRequestBuilder getTransmissionRequestBuilder() {
        return injector.getInstance(TransmissionRequestBuilder.class);
    }

    /**
     * Retrieves instance of Transmitter, without revealing intern object dependency injection.
     * @return instance of Transmitter
     */
    public Transmitter getTransmitter() {
        return injector.getInstance(Transmitter.class);
    }

    /**
     * Retrieves instance of SmpLookupManager, without revealing intern object dependency injection.
     * @return
     */
    public SmpLookupManager getSmpLookupManager() {
        return injector.getInstance(SmpLookupManager.class);
    }

}