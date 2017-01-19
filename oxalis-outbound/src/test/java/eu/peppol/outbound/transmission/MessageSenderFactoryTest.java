package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.as2.outbound.As2OutboundModule;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.commons.http.ApacheHttpModule;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.timestamp.TimestampModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = {TransmissionTestModule.class, ModeModule.class, As2OutboundModule.class, TracingModule.class,
        TimestampModule.class, ApacheHttpModule.class})
public class MessageSenderFactoryTest {

    @Inject
    private MessageSenderFactory messageSenderFactory;

    @Test
    public void simple() throws OxalisTransmissionException {
        Assert.assertEquals(messageSenderFactory.getPrioritizedTransportProfiles().size(), 3);
        Assert.assertEquals(messageSenderFactory.getSender(TransportProfile.AS2_1_0), "oxalis-as2");
        Assert.assertEquals(messageSenderFactory.getSender(TransportProfile.of("busdox-transport-dummy")), "dummy");
    }

    @Test
    public void validTransportProfile() throws OxalisTransmissionException {
        Assert.assertNotNull(messageSenderFactory.getMessageSender(TransportProfile.AS2_1_0));
    }

    @Test(expectedExceptions = OxalisTransmissionException.class)
    public void invalidTransportProfile() throws OxalisTransmissionException {
        messageSenderFactory.getMessageSender(TransportProfile.START);
    }
}
