package eu.peppol.outbound.lookup;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.lang.OxalisTransmissionException;
import eu.peppol.outbound.As2PrioritizedTransportModule;
import eu.peppol.outbound.statistics.StatisticsModule;
import eu.peppol.outbound.transmission.TransmissionTestModule;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import no.difi.vefa.peppol.common.model.*;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = {As2PrioritizedTransportModule.class, LookupModule.class, TracingModule.class, ModeModule.class, TransmissionTestModule.class, StatisticsModule.class})
public class CachedLookupServiceTest {

    private static ParticipantIdentifier participant = ParticipantIdentifier.of("9908:810418052");
    private static DocumentTypeIdentifier documenttype = DocumentTypeIdentifier.of("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol4a:ver2.0::2.1");
    private static ProcessIdentifier process = ProcessIdentifier.of("urn:www.cenbii.eu:profile:bii04:ver2.0");

    @Inject
    @Named("cached")
    private LookupService lookupService;

    @Test
    public void simple() throws Exception {
        Endpoint endpoint = lookupService.lookup(Header.newInstance()
                .receiver(participant)
                .documentType(documenttype)
                .process(process));

        Assert.assertNotNull(endpoint);
    }

    @Test
    public void simpleCached() throws Exception {
        Endpoint endpoint1 = lookupService.lookup(Header.newInstance()
                .receiver(participant)
                .documentType(documenttype)
                .process(process));

        Endpoint endpoint2 = lookupService.lookup(Header.newInstance()
                .receiver(participant)
                .documentType(documenttype)
                .process(process));

        Assert.assertTrue(endpoint1 == endpoint2);
    }

    @Test(expectedExceptions = OxalisTransmissionException.class)
    public void triggerException() throws Exception {
        lookupService.lookup(Header.newInstance()
                .receiver(ParticipantIdentifier.of("9908:---"))
                .documentType(documenttype)
                .process(process));
    }

    @Test
    public void simpleHeaderStub() {
        CachedLookupService.HeaderStub headerStub = new CachedLookupService.HeaderStub(Header.newInstance()
        .receiver(participant)
        .documentType(documenttype)
        .process(process));

        Assert.assertTrue(headerStub.equals(headerStub));
        Assert.assertFalse(headerStub.equals(null));
        Assert.assertFalse(headerStub.equals(new Object()));

        Assert.assertTrue(headerStub.equals(new CachedLookupService.HeaderStub(Header.newInstance()
                .receiver(participant)
                .documentType(documenttype)
                .process(process))));
        Assert.assertFalse(headerStub.equals(new CachedLookupService.HeaderStub(Header.newInstance()
                .receiver(ParticipantIdentifier.of("9908:---"))
                .documentType(documenttype)
                .process(process))));
        Assert.assertFalse(headerStub.equals(new CachedLookupService.HeaderStub(Header.newInstance()
                .receiver(participant)
                .documentType(DocumentTypeIdentifier.of("---"))
                .process(process))));
        Assert.assertFalse(headerStub.equals(new CachedLookupService.HeaderStub(Header.newInstance()
                .receiver(participant)
                .documentType(documenttype)
                .process(ProcessIdentifier.of("---")))));
    }
}
