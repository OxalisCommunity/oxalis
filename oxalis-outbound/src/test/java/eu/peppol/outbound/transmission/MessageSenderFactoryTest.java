package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.Injector;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.vefa.peppol.common.model.TransportProfile;
import no.difi.vefa.peppol.mode.Mode;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = {TransmissionTestModule.class, ModeModule.class})
public class MessageSenderFactoryTest {

    @Inject
    private Mode mode;

    @Inject
    private Injector injector;

    @Test
    public void simple() throws OxalisTransmissionException {
        MessageSenderFactory messageSenderFactory = new MessageSenderFactory(injector, mode);

        Assert.assertEquals(messageSenderFactory.getPrioritizedTransportProfiles().size(), 2);
        Assert.assertEquals(messageSenderFactory.getSender(TransportProfile.AS2_1_0), "oxalis-as2");
    }
}
