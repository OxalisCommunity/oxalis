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

package eu.peppol;


import java.util.Arrays;
import java.util.Base64;

/**
 * Holds the digest and the algorithm name for some arbitrary contents like for instance the payload of a message, the message itself etc.
 *
 * @author steinar
 *         Date: 31.10.2015
 *         Time: 12.49
 */
public class MessageDigestResult {

    String digestAsString;
    byte[] digest;
    String algorithmName;

    public MessageDigestResult(byte[] digest, String algorithmName) {
        this.digest = digest;
        this.algorithmName = algorithmName;
    }


    public String getDigestAsString() {
        return new String(Base64.getEncoder().encode(digest));
    }

    public byte[] getDigest() {
        return digest;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageDigestResult{");
        sb.append("digestAsString='").append(digestAsString).append('\'');
        sb.append(", digest=").append(Arrays.toString(digest));
        sb.append(", algorithmName='").append(algorithmName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
