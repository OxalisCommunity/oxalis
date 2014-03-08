/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
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

package eu.peppol.smp;

import eu.peppol.util.ConnectionException;
import eu.peppol.util.TryAgainLaterException;
import org.xml.sax.InputSource;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * @author steinar
 *         Date: 18.12.13
 *         Time: 22:34
 */
public class SmpContentRetrieverImpl implements SmpContentRetriever {

    private static final String ENCODING_GZIP = "gzip";
    private static final String ENCODING_DEFLATE = "deflate";

    /**
     * Gets the XML content of a given url, wrapped in an InputSource object.
     */
    @Override
    public InputSource getUrlContent(URL url) {

        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to connect to " + url + " ; " + e.getMessage(), e);
        }

        try {
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_UNAVAILABLE)
                throw new TryAgainLaterException(url, httpURLConnection.getHeaderField("Retry-After"));
            if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
                throw new ConnectionException(url, httpURLConnection.getResponseCode());
        } catch (IOException e) {
            throw new RuntimeException("Problem reading URL data at " + url.toExternalForm(), e);
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

            String xml = readInputStreamIntoString(result);

            return new InputSource(new StringReader(xml));

        } catch (Exception e) {
            throw new RuntimeException("Problem reading URL data at " + url.toExternalForm(), e);
        }

    }

    String readInputStreamIntoString(InputStream result) {
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(result, Charset.forName("UTF-8")));
        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read data from InputStream " + e, e);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                // Ignore any problems related to closing of input stream
            }
        }
        return sb.toString();
    }


}
