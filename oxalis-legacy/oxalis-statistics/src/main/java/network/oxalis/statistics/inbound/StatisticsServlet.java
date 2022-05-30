/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.statistics.inbound;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import network.oxalis.statistics.api.StatisticsGranularity;
import network.oxalis.statistics.security.OxalisCipher;
import network.oxalis.statistics.security.OxalisCipherConverter;
import network.oxalis.statistics.security.StatisticsKeyTool;
import org.joda.time.DateTime;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;

/**
 * There are only 3 parameters, start, end and granularity - like this :
 * {@literal https://your.accesspoint.com/oxalis/statistics?start=2013-01-01T00&end=2014-02-01T00&granularity=H}
 * <p>
 * The start/end are dates are ISO formatted like : yyyy-mm-ddThh
 * The granularity can be H (hour), D (day), M (month) and Y (year), for reference {@link StatisticsGranularity}
 *
 * @author steinar
 * @author thore
 */
@Singleton
public class StatisticsServlet extends HttpServlet {

    @Inject
    private StatisticsProducer statisticsProducer;

    private PublicKey publicKey;

    @Inject
    private StatisticsKeyTool statisticsKeyTool;

    @Override
    public void init(ServletConfig servletConfig) {
        // Loads our asymmetric public key
        publicKey = statisticsKeyTool.loadPublicKeyFromClassPath();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.getOutputStream().write("Hello!\nOxalis statistics does not support http POST".getBytes());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();

        Params params = parseParams(parameterMap);

        // Need the output stream for emission of XML
        ServletOutputStream servletOutputStream = response.getOutputStream();

        // Encryption of the output stream
        OxalisCipher oxalisCipher = new OxalisCipher();

        // Returns the symmetric key used in the Cipher, wrapped with the public key
        String wrappedSymmetricKeyAsString = new OxalisCipherConverter()
                .getWrappedSymmetricKeyAsString(publicKey, oxalisCipher);
        response.setHeader(OxalisCipher.WRAPPED_SYMMETRIC_KEY_HEADER_NAME, wrappedSymmetricKeyAsString);

        // wraps the servlet output stream with encryption
        OutputStream encryptedOutputStream = oxalisCipher.encryptStream(servletOutputStream);

        // Retrieves the data from the DBMS and emits the XML thru the encryped stream
        statisticsProducer.emitData(encryptedOutputStream, params.start, params.end, params.granularity);

        encryptedOutputStream.close();
    }

    Params parseParams(Map<String, String[]> parameterMap) {
        Params result = new Params();
        parseGranularity(parameterMap, result);
        parseDates(parameterMap, result);
        return result;
    }

    private void parseDates(Map<String, String[]> parameterMap, Params result) {
        result.start = parseDate(getParamFromMultiValues(parameterMap, "start"));
        result.end = parseDate(getParamFromMultiValues(parameterMap, "end"));
    }

    private void parseGranularity(Map<String, String[]> parameterMap, Params result) {
        String granularity = getParamFromMultiValues(parameterMap, "g");
        if (granularity == null) {
            granularity = getParamFromMultiValues(parameterMap, "granularity");
        }
        if (granularity == null) {
            throw new IllegalArgumentException("Missing request parameter: 'granularity' (Y,M,D or H)");
        } else {
            result.granularity = StatisticsGranularity.valueForAbbreviation(granularity);
        }
    }

    String getParamFromMultiValues(Map<String, String[]> parameterMap, String key) {
        String[] values = parameterMap.get(key);
        if (values != null && values.length > 0) {
            return values[0];
        } else {
            return null;
        }
    }

    private Date parseDate(String dateAsString) {
        if (dateAsString != null) {
            try {
                // JODA time is really the king of date and time parsing :-)
                DateTime date = DateTime.parse(dateAsString);
                return date.toDate();
            } catch (Exception e) {
                throw new IllegalStateException(String.format(
                        "Unable to parseMultipart '%s'into a date and time using ISO8601 pattern YYYY-MM-DD HH",
                        dateAsString));
            }
        }
        return null;
    }

    static class Params {
        Date start;

        Date end;

        StatisticsGranularity granularity;
    }
}
