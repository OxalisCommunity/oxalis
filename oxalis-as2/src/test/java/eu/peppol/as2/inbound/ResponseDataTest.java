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

package eu.peppol.as2.inbound;

import com.google.inject.Inject;
import eu.peppol.as2.model.MdnData;
import eu.peppol.as2.model.Mic;
import eu.peppol.as2.util.MdnMimeMessageFactory;
import eu.peppol.as2.util.TestDataGenerator;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.testng.Assert.assertNotNull;

@Guice(modules = {GuiceModuleLoader.class})
public class ResponseDataTest {

    @Inject
    TestDataGenerator testDataGenerator;

    MdnMimeMessageFactory mdnMimeMessageFactory;

    @Inject
    private PrivateKey privateKey;

    @Inject
    private X509Certificate certificate;

    @BeforeMethod
    public void setUp() {
        mdnMimeMessageFactory = new MdnMimeMessageFactory(certificate, privateKey);
    }

    @Test()
    public void createResponsData() throws Exception {
        assertNotNull(testDataGenerator);
        InternetHeaders sampleInternetHeaders = testDataGenerator.createSampleInternetHeaders();
        InputStream inputStream = testDataGenerator.loadSbdhAsicXml();

        Mic mic = new Mic("jablajabla", "SHA-1");

        MdnData mdnData = MdnData.Builder.buildProcessedOK(sampleInternetHeaders, mic);

        MimeMessage signedMdn = mdnMimeMessageFactory.createSignedMdn(mdnData, sampleInternetHeaders);
        ResponseData responseData = new ResponseData(200, signedMdn, mdnData);
    }
}