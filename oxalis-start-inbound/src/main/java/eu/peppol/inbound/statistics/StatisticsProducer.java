package eu.peppol.inbound.statistics;

import eu.peppol.statistics.StatisticsRepository;
import eu.peppol.statistics.StatisticsToXmlTransformer;

import javax.servlet.ServletOutputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * User: steinar
 * Date: 23.02.13
 * Time: 21:49
 */
public class StatisticsProducer {

    private final StatisticsRepository statisticsRepository;

    public StatisticsProducer(StatisticsRepository statisticsRepository) {

        this.statisticsRepository = statisticsRepository;
    }

    public void emitData(OutputStream outputStream, Date start, Date end) {
        StatisticsToXmlTransformer statisticsToXmlTransformer = new StatisticsToXmlTransformer(outputStream);

        statisticsRepository.fetchAndTransform(statisticsToXmlTransformer, start, end);

    }
}
