/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.as2.evidence;

import com.google.inject.Inject;
import eu.peppol.as2.As2TestModule;
import eu.peppol.evidence.TransmissionEvidence;
import eu.peppol.evidence.TransmissionEvidenceTransformer;
import no.difi.vefa.peppol.common.util.DomUtils;
import no.difi.vefa.peppol.security.xmldsig.XmldsigVerifier;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 20.01.2016
 *         Time: 14.56
 */
@Test(groups = {"integration"})
@Guice(modules = { As2TestModule.class})
public class TransmissionEvidenceTransformerAs2WithRemImplTest {

    @Inject
    SampleTransmissionEvidenceGenerator sampleTransmissionEvidenceGenerator;



    @Test
    public void loadTransmissionEvidenceTransformerInstance() throws Exception {

        TransmissionEvidenceTransformer transformer = TransmissionEvidenceTransformerAs2WithRemImpl.INSTANCE;

        assertNotNull(transformer);

        TransmissionEvidence sample = sampleTransmissionEvidenceGenerator.createSampleTransmissionEvidenceWithRemAndMdn();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        IOUtils.copy(transformer.getInputStream(sample), outputStream);

        System.out.println(outputStream.toString("UTF-8"));

        java.security.cert.X509Certificate x509Certificate = XmldsigVerifier.verify(DomUtils.parse(new ByteArrayInputStream(outputStream.toByteArray())));
    }
}