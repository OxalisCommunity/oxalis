package eu.peppol.inbound.statistics;

import eu.peppol.statistics.StatisticsGranularity;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.StatisticsToXmlTransformer;

import java.io.OutputStream;
import java.util.Date;

/**
 * User: steinar
 * Date: 23.02.13
 * Time: 21:49
 */
public class StatisticsProducer {

    private final RawStatisticsRepository rawStatisticsRepository;

    public StatisticsProducer(RawStatisticsRepository rawStatisticsRepository) {

        this.rawStatisticsRepository = rawStatisticsRepository;
    }

    public void emitData(OutputStream outputStream, Date start, Date end, StatisticsGranularity granularity) {
        StatisticsToXmlTransformer statisticsToXmlTransformer = new StatisticsToXmlTransformer(outputStream);

        rawStatisticsRepository.fetchAndTransformRawStatistics(statisticsToXmlTransformer, start, end, granularity);
    }
}
