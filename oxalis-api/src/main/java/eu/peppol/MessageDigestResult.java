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
