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
import eu.peppol.util.OxalisConstant;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.ManifestItem;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Calculates the digest of the payload held inside the SBD.
 *
 * @author steinar
 *         Date: 11.01.2016
 *         Time: 15.24
 */
public class PayloadDigestCalculator {


    public static MessageDigestResult calcDigest(String algorithm, StandardBusinessDocumentHeader sbdh, InputStream inputStream) {
        MessageDigest messageDigest;

        try {
            messageDigest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unknown digest algorithm " + OxalisConstant.DEFAULT_DIGEST_ALGORITHM + " " + e.getMessage(), e);
        }


        InputStream inputStreamToCalculateDigestFrom = null;

        ManifestItem manifestItem = SbdhFastParser.searchForAsicManifestItem(sbdh);
        if (manifestItem != null) {
            // creates an FilterInputStream, which will extract the ASiC in binary format.
            inputStreamToCalculateDigestFrom = new Base64InputStream(new AsicFilterInputStream(inputStream));
        } else
            inputStreamToCalculateDigestFrom = inputStream;


        DigestInputStream digestInputStream = new DigestInputStream(new BufferedInputStream(inputStreamToCalculateDigestFrom), messageDigest);
        try {
            IOUtils.copy(digestInputStream, new NullOutputStream());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to calculate digest for payload");
        }

        return new MessageDigestResult(messageDigest.digest(), messageDigest.getAlgorithm());
    }
}
