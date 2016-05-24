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

package eu.peppol.document;

import eu.peppol.MessageDigestResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import java.io.InputStream;
import java.util.Base64;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 11.01.2016
 *         Time: 15.24
 */
public class PayloadDigestCalculatorTest {

    public static final Logger log = LoggerFactory.getLogger(PayloadDigestCalculatorTest.class);

    @Test
    public void calcDigestForAsic() throws Exception {

        InputStream resourceAsStream = loadSampleSbdWithAsic();

        SbdhFastParser sbdhFastParser = new SbdhFastParser();
        StandardBusinessDocumentHeader sbdh = sbdhFastParser.parse(resourceAsStream);

        MessageDigestResult result = PayloadDigestCalculator.calcDigest("SHA-256",sbdh, loadSampleSbdWithAsic());

        log.debug("Calculated digest: " + new String(Base64.getEncoder().encode(result.getDigest())));

    }

    @NotNull
    protected InputStream loadSampleSbdWithAsic() {
        String resourceName = "sample-sbd-with-asic.xml";
        InputStream resourceAsStream = PayloadDigestCalculatorTest.class.getClassLoader().getResourceAsStream(resourceName);
        assertNotNull(resourceAsStream, resourceName + " not found in class path");
        return resourceAsStream;
    }
}