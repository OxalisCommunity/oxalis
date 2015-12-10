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

package eu.peppol.as2.evidence;

import com.google.inject.Inject;
import eu.peppol.persistence.TransmissionEvidence;
import eu.peppol.security.SecurityModule;
import eu.peppol.util.RuntimeConfigurationModule;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 20.11.2015
 *         Time: 16.51
 */
@Test(groups = {"integration"})
@Guice(modules = {TransportEvidenceModule.class, SecurityModule.class, RuntimeConfigurationModule.class})
public class As2TransmissionEvidenceFormatterIT {


    @Inject
    SampleTransmissionEvidenceGenerator sampleTransmissionEvidenceGenerator;

    @Inject
    As2TransmissionEvidenceFormatter formatter;

    @Test
    public void testFormat() throws Exception {

        assertNotNull(sampleTransmissionEvidenceGenerator);
        TransmissionEvidence sample = sampleTransmissionEvidenceGenerator.createSampleTransmissionEvidenceWithRemAndMdn();

        assertNotNull(formatter, "Seems something went wrong with dependency injection, field 'formatter' is null");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        formatter.format(sample, outputStream);

        System.out.println(outputStream.toString("UTF-8"));

        // X509Certificate x509Certificate = XmldsigVerifier.verify(DomUtils.parse(new ByteArrayInputStream(outputStream.toByteArray())));

//         System.out.println(x509Certificate);

    }



}