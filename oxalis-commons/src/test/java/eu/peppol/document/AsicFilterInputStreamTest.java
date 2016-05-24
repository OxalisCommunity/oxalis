/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Public Government and eGovernment (Difi)
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

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Parses an SBD containing SBDH + base64 encoded ASiC archive.
 * <p>
 * The ASiC archive is decoded and separated into a separate file.
 *
 * @author steinar
 *         Date: 23.12.2015
 *         Time: 16.37
 */
public class AsicFilterInputStreamTest {

    public static final Logger log = LoggerFactory.getLogger(AsicFilterInputStreamTest.class);

    /**
     * Takes a file holding an SBD/SBDH with an ASiC archive in base64 as payload and extracts the ASiC archive in binary format, while
     * calculating the message digest.
     *
     * @throws Exception
     */
    @Test
    public void parseSampleSbdWithAsic() throws Exception {
        InputStream resourceAsStream = AsicFilterInputStreamTest.class.getClassLoader().getResourceAsStream("sample-sbd-with-asic.xml");

        AsicFilterInputStream asicFilterInputStream = new AsicFilterInputStream(resourceAsStream);

        Path asicFile = Files.createTempFile("unit-test", ".asice");
        OutputStream asicOutputStream = Files.newOutputStream(asicFile);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        DigestOutputStream digestOutputStream = new DigestOutputStream(asicOutputStream, messageDigest);
        Base64OutputStream base64OutputStream = new Base64OutputStream(digestOutputStream, false);

        IOUtils.copy(asicFilterInputStream, base64OutputStream);
        log.debug("Wrote ASiC to:" + asicFile);
        log.debug("Digest: " + new String(Base64.getEncoder().encode(messageDigest.digest())));

    }


}
