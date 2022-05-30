/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package network.oxalis.outbound.transmission;

import com.google.inject.Inject;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.commons.guice.GuiceModuleLoader;
import network.oxalis.test.asd.AsdConstants;
import network.oxalis.vefa.peppol.common.model.TransportProfile;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = GuiceModuleLoader.class)
public class MessageSenderFactoryTest {

    @Inject
    private MessageSenderFactory messageSenderFactory;

    @Test
    public void simple() throws OxalisTransmissionException {
        Assert.assertTrue(messageSenderFactory.getPrioritizedTransportProfiles().size() > 0);
        Assert.assertEquals(messageSenderFactory.getSender(TransportProfile.of("bdx-transport-asd")), "oxalis-asd");
    }

    @Test
    public void validTransportProfile() throws OxalisTransmissionException {
        Assert.assertNotNull(messageSenderFactory.getMessageSender(AsdConstants.TRANSPORT_PROFILE));
    }

    @Test(expectedExceptions = OxalisTransmissionException.class)
    public void invalidTransportProfile() throws OxalisTransmissionException {
        messageSenderFactory.getMessageSender(TransportProfile.START);
    }
}
