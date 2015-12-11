/*
 * Copyright (c) 2015 Steinar Overbeck Cook
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.as2;

import com.google.inject.Inject;
import eu.peppol.document.SbdhFastParser;
import eu.peppol.security.KeystoreManager;
import eu.peppol.util.RuntimeConfigurationModule;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.mail.internet.InternetHeaders;

/**
 * @author steinar
 *         Date: 08.12.2015
 *         Time: 15.21
 */

@Guice(modules = {RuntimeConfigurationModule.class})

public class InboundMessageReceiverTest {


    private InternetHeaders headers;
    private String ourCommonName = "APP_1000000135";


    @Inject
    KeystoreManager keystoreManager;

    @BeforeClass
    public void setUp(){
        headers = new InternetHeaders();
        headers.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName(), "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        headers.addHeader(As2Header.AS2_TO.getHttpHeaderName(), PeppolAs2SystemIdentifier.AS2_SYSTEM_ID_PREFIX + ourCommonName.toString());
        headers.addHeader(As2Header.AS2_FROM.getHttpHeaderName(), PeppolAs2SystemIdentifier.AS2_SYSTEM_ID_PREFIX + ourCommonName.toString());
        headers.addHeader(As2Header.MESSAGE_ID.getHttpHeaderName(), "42");
        headers.addHeader(As2Header.AS2_VERSION.getHttpHeaderName(), As2Header.VERSION);
        headers.addHeader(As2Header.SUBJECT.getHttpHeaderName(), "An AS2 message");
        headers.addHeader(As2Header.DATE.getHttpHeaderName(), "Mon Oct 21 22:01:48 CEST 2013");

    }
    @Test
    public void testReceive() throws Exception {

        InboundMessageReceiver inboundMessageReceiver = new InboundMessageReceiver(new SbdhFastParser(), new As2MessageInspector(keystoreManager));

//        inboundMessageReceiver.receive(headers,)


    }
}