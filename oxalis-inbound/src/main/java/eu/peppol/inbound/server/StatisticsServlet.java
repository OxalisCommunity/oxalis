package eu.peppol.inbound.server;

import eu.peppol.inbound.statistics.StatisticsProducer;
import eu.peppol.security.OxalisCipher;
import eu.peppol.security.OxalisCipherConverter;
import eu.peppol.security.StatisticsKeyTool;
import eu.peppol.statistics.RawStatisticsRepositoryFactory;
import eu.peppol.statistics.RawStatisticsRepositoryFactoryProvider;
import eu.peppol.statistics.StatisticsGranularity;
import eu.peppol.statistics.RawStatisticsRepository;
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
 * User: steinar
 * Date: 17.12.12
 * Time: 16:37
 */
public class StatisticsServlet extends HttpServlet {

    private RawStatisticsRepository rawStatisticsRepository;
    private PublicKey publicKey;

    @Override
    public void init(ServletConfig servletConfig) {
        RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory = RawStatisticsRepositoryFactoryProvider.getInstance();
        rawStatisticsRepository = rawStatisticsRepositoryFactory.getInstanceForRawStatistics();
        // Loads our asymmetric public key
        publicKey = new StatisticsKeyTool().loadPublicKeyFromClassPath();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getOutputStream().write("Hello!\nOxalis statistics does not support http POST".getBytes());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        @SuppressWarnings("unchecked") Map<String, String[]> parameterMap = (Map<String, String[]>)request.getParameterMap();

        Params params = parseParams(parameterMap);


        StatisticsProducer statisticsProducer = new StatisticsProducer(rawStatisticsRepository);
        // Need the output stream for emission of XML
        ServletOutputStream servletOutputStream = response.getOutputStream();

        // Encrypts the output stream
        OxalisCipher oxalisCipher = new OxalisCipher();
        // Returns the symmetric key used in the Cipher, wrapped with the public key
        String wrappedSymmetricKeyAsString = new OxalisCipherConverter().getWrappedSymmetricKeyAsString(publicKey, oxalisCipher);
        response.setHeader(OxalisCipher.WRAPPED_SYMMETRIC_KEY_HEADER_NAME, wrappedSymmetricKeyAsString);


        OutputStream encryptedOutputStream = oxalisCipher.encryptStream(servletOutputStream);

        // Retrieves the data from the DBMS and emits the XML
        //
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
        Date result = null;
        if (dateAsString != null) {
            try {
                // JODA time is really the king of date and time parsing :-)
                DateTime date = DateTime.parse(dateAsString);
                return date.toDate();
            } catch (Exception e) {
                throw new IllegalStateException("Unable to parseMultipart " + dateAsString + " into a date and time using ISO8601 pattern YYYY-MM-DD HH");
            }
        }

        return result;
    }

    static class Params {
        Date start, end;
        StatisticsGranularity granularity;
    }
}
