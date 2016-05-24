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

package eu.peppol.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * User: nigel
 * Date: Oct 25, 2011
 * Time: 11:08:22 PM
 */
public class Util {

    private static final String ALGORITHM_MD5 = "MD5";
    private static final String ALGORITHM_SHA256 = "SHA-256";
    public static final int DEFAULT_BUFFER_SIZE = 4 * 1024;

    public static String calculateMD5(String value) throws MessageDigestException {

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(ALGORITHM_MD5);
        } catch (NoSuchAlgorithmException e) {
            throw new MessageDigestException(value, e);
        }
        messageDigest.reset();
        try {
            messageDigest.update(value.getBytes("iso-8859-1"), 0, value.length());
        } catch (UnsupportedEncodingException e) {
            throw new MessageDigestException(value, e);

        }
        byte[] digest = messageDigest.digest();
        StringBuilder sb = new StringBuilder();

        for (byte b : digest) {
            String hex = Integer.toHexString(0xFF & b);

            if (hex.length() == 1) {
                sb.append('0');
            }

            sb.append(hex);
        }

        return sb.toString();
    }

    public static byte[] calculateSHA256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = null;
        messageDigest = MessageDigest.getInstance(ALGORITHM_SHA256);
        messageDigest.update(data, 0, data.length);
        return messageDigest.digest();
    }

    public static byte[] intoBuffer(InputStream inputStream, long maxBytes) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int numberOfBytesRead = 0;
        long byteCount = 0;

        while ((numberOfBytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, numberOfBytesRead);
            byteCount += numberOfBytesRead;
            if (byteCount > maxBytes) {
                throw new IllegalStateException("Inputdata exceeded threshold of " + maxBytes);
            }
        }

        return byteArrayOutputStream.toByteArray();
    }

}
