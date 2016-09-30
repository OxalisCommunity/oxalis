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

package eu.peppol.inbound.statistics;

import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.StatisticsGranularity;
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
