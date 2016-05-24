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

package eu.peppol.statistics;

import java.util.Date;

/**
 * Objects implementing this interface are capable of storing and retrieving raw data
 * pertaining to the sending and receiving of PEPPOL messages in order to provide statistics.
 *
 * User: steinar
 * Date: 30.01.13
 * Time: 19:28
 */
public interface RawStatisticsRepository {

    /**
     * Persists another raw statistics entry into table {@code raw_stats}
     * */
    Integer persist(RawStatistics rawStatistics);

    /**
     * Retrieves data from table <code>raw_stats</code> and transforms it into an appropriate XML document
     */
    void fetchAndTransformRawStatistics(StatisticsTransformer transformer, Date start, Date end, StatisticsGranularity granularity);

}
