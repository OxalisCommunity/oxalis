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

package eu.peppol.smp;

import eu.peppol.util.ConnectionException;
import eu.peppol.util.TryAgainLaterException;
import org.apache.commons.io.input.BOMInputStream;
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
            InputStream in = new BOMInputStream(httpURLConnection.getInputStream());
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
