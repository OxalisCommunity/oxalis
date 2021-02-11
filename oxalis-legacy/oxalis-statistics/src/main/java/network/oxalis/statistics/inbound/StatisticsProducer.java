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
import network.oxalis.statistics.api.RawStatisticsRepository;
import network.oxalis.statistics.api.StatisticsGranularity;
import network.oxalis.statistics.util.StatisticsToXmlTransformer;

import java.io.OutputStream;
import java.util.Date;

/**
 * User: steinar
 * Date: 23.02.13
 * Time: 21:49
 */
public class StatisticsProducer {

    private final RawStatisticsRepository rawStatisticsRepository;

    @Inject
    public StatisticsProducer(RawStatisticsRepository rawStatisticsRepository) {
        this.rawStatisticsRepository = rawStatisticsRepository;
    }

    public void emitData(OutputStream outputStream, Date start, Date end, StatisticsGranularity granularity) {
        StatisticsToXmlTransformer statisticsToXmlTransformer = new StatisticsToXmlTransformer(outputStream);

        rawStatisticsRepository.fetchAndTransformRawStatistics(statisticsToXmlTransformer, start, end, granularity);
    }
}
