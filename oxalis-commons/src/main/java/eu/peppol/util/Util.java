package eu.peppol.util;

import org.xml.sax.InputSource;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * User: nigel
 * Date: Oct 25, 2011
 * Time: 11:08:22 PM
 */
public class Util {

    private static final String ENCODING_GZIP = "gzip";
    private static final String ENCODING_DEFLATE = "deflate";
    private static final String ALGORITHM_MD5 = "MD5";

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
            throw new MessageDigestException(value,e);

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

    /**
     * Gets the content of a given url.
     */
    public static InputSource getUrlContent(URL url) {

        BufferedReader bufferedReader = null;
        StringBuilder sb = new StringBuilder();

        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to connect to " + url +" ; " + e.getMessage(), e);
        }

        try {
            String encoding = httpURLConnection.getContentEncoding();
            InputStream in = httpURLConnection.getInputStream();
            InputStream result;

            if (encoding != null && encoding.equalsIgnoreCase(ENCODING_GZIP)) {
                result = new GZIPInputStream(in);
            } else if (encoding != null && encoding.equalsIgnoreCase(ENCODING_DEFLATE)) {
                result = new InflaterInputStream(in);
            } else {
                result = in;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(result));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

        } catch (Exception e) {
            throw new RuntimeException("Problem reading SMP data at " + url.toExternalForm(), e);
        } finally {
            try {
                //noinspection ConstantConditions
                bufferedReader.close();
            } catch (Exception e) {
            }
        }

        String xml = sb.toString();
        return new InputSource(new StringReader(xml));
    }
}
