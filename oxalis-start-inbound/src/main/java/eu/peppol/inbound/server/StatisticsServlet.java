package eu.peppol.inbound.server;

import eu.peppol.inbound.statistics.StatisticsProducer;
import eu.peppol.statistics.StatisticsRepository;
import eu.peppol.statistics.StatisticsRepositoryFactory;
import eu.peppol.statistics.StatisticsRepositoryFactoryProvider;
import org.joda.time.DateTime;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: steinar
 * Date: 17.12.12
 * Time: 16:37
 */
public class StatisticsServlet extends HttpServlet {

    private StatisticsRepository statisticsRepository;

    @Override
    public void init(ServletConfig servletConfig) {
        StatisticsRepositoryFactory statisticsRepositoryFactory = StatisticsRepositoryFactoryProvider.getInstance();
        statisticsRepository = statisticsRepositoryFactory.getInstance();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getOutputStream().write("Hello!\nOxalis statistics does not support http POST".getBytes());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ServletOutputStream outputStream = response.getOutputStream();

        String start = request.getParameter("start");
        String end = request.getParameter("end");


        Date startDate = parseDate(outputStream, start);
        Date endDate = parseDate(outputStream, end);

        StatisticsProducer statisticsProducer = new StatisticsProducer(statisticsRepository);
        statisticsProducer.emitData(outputStream, startDate, endDate);

        outputStream.flush();
    }

    private Date parseDate(ServletOutputStream outputStream, String dateAsString) throws IOException {
        Date result = null;
        if (dateAsString != null){
            try {
                // JODA time is really the king of date and time parsing :-)
                DateTime date = DateTime.parse(dateAsString);
                return date.toDate();
            } catch (Exception e) {
                outputStream.write(("Unable to parse " + dateAsString + " into a date and time using ISO8601 pattern YYYY-MM-DD HH:MM:SS\n").getBytes());
                throw new IllegalStateException("Unable to parse date parameters");
            }
        }

        return result;
    }
}
