package eu.peppol.util;

import java.io.*;
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
