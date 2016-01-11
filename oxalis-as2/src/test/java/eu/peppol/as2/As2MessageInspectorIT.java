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

import com.google.inject.Inject;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import eu.peppol.util.OxalisCommonsModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.mail.internet.MimeMessage;
import javax.security.auth.x500.X500Principal;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 08.10.13
 *         Time: 11:10
 */
@Test(groups = "integration")
@Guice(modules = {OxalisCommonsModule.class})
public class As2MessageInspectorIT {

    @Inject
    KeystoreManager keystoreManager;

    @Inject
    As2MessageInspector as2MessageInspector;

    // Created by the setUp() method
    private As2Message as2Message;

    @BeforeMethod
    public void setUp() throws Exception {

        // We must supply our certificate as part of the signature for validation
        X509Certificate ourCertificate = keystoreManager.getOurCertificate();

        // Obtains our private key for the actual signature of the message
        PrivateKey ourPrivateKey = keystoreManager.getOurPrivateKey();

        // Fetch input stream for sample data
        InputStream resourceAsStream = As2MessageInspectorIT.class.getClassLoader().getResourceAsStream("example.xml");
        assertNotNull(resourceAsStream);

        // The content-type must be manually specified as there is no way of automatically probing the file.
        MimeType mimeType = new MimeType("application", "xml");

        // Creates the S/MIME message
        SMimeMessageFactory SMimeMessageFactory = new SMimeMessageFactory(ourPrivateKey, ourCertificate);
        MimeMessage mimeMessage = SMimeMessageFactory.createSignedMimeMessage(resourceAsStream, mimeType);
        assertNotNull(mimeMessage);

        // Finally we add the required headers
        As2Message.Builder builder = new As2Message.Builder(new SignedMimeMessage(mimeMessage));

        X500Principal subjectX500Principal = ourCertificate.getSubjectX500Principal();

        CommonName commonName = CommonName.valueOf(subjectX500Principal);

        builder.as2To(PeppolAs2SystemIdentifier.valueOf(commonName));
        builder.as2From(PeppolAs2SystemIdentifier.valueOf(commonName));
        builder.transmissionId("42");
        builder.date(new Date());
        builder.subject("PEPPOL Message");

        as2Message = builder.build();
    }

    /**
     * Validates the AS2 Message created in the set up
     *
     * @throws Exception
     */
    @Test
    public void validateAs2Message() throws Exception {

        as2MessageInspector.validate(as2Message);
    }
}
