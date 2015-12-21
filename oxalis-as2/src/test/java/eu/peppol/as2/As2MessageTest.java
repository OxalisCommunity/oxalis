/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.as2;

import eu.peppol.security.KeystoreManager;
import eu.peppol.security.KeystoreManagerImpl;
import eu.peppol.security.PeppolKeystoreLoader;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.UnitTestGlobalConfigurationImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 28.10.13
 *         Time: 12:08
 */
public class As2MessageTest {


    private MimeMessage signedMimeMessage;

    KeystoreManager keystoreManager;
    private X509Certificate ourCertificate;
    private PrivateKey ourPrivateKey;


    @BeforeMethod
    public void setUp() throws Exception {
        GlobalConfiguration globalConfiguration = UnitTestGlobalConfigurationImpl.createInstance();

        keystoreManager = new KeystoreManagerImpl(globalConfiguration, new PeppolKeystoreLoader(globalConfiguration));

        ourCertificate = keystoreManager.getOurCertificate();
        ourPrivateKey = keystoreManager.getOurPrivateKey();


    }

    @Test
    public void createSampleSmimeMessage() throws Exception {

        SMimeMessageFactory SMimeMessageFactory = new SMimeMessageFactory(ourPrivateKey, ourCertificate);

        InputStream resourceAsStream = As2MessageTest.class.getResourceAsStream("/as2-peppol-bis-invoice-sbdh.xml");
        assertNotNull(resourceAsStream);

        signedMimeMessage = SMimeMessageFactory.createSignedMimeMessage(resourceAsStream, new MimeType("application/xml"));


    }
}
