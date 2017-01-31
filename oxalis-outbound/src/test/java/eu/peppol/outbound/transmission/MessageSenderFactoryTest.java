/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = GuiceModuleLoader.class)
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
