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
 * Transforms aggregated statistics into another format. This interface has been designed to maximize performance
 * when transforming the results from the statistics repository into whatever format the implementor of this
 * interface has choosen. I.e. transformation from SQL ResultSet to XML, which will not require any new objects to
 * be created.
 *
 * <p>To use it:
 * <ol>
 *     <li>Invoke {@link #startEntry()}  first</li>
 *     <li>Invoke the writeXxxx methods for each field</li>
 *     <li>Complete the entry by calling {@link #endEntry()}</li>
 * </ol></p>
 *
 * User: steinar
 * Date: 24.02.13
 * Time: 10:27
 */
public interface StatisticsTransformer {

    String STATISTICS_DOCUMENT_START_ELEMENT_NAME = "peppol-ap-statistics";
    String ENTRY_START_ELEMENT_NAME = "entry";
    String ACCESS_POINT_ID_ELEMENT_NAME = "access-point-id";
    String PARTICIPANT_ID_ELEMENT_NAME = "participant-id";
    String DOCUMENT_TYPE_ELEMENT_NAME = "document-type";
    String PROFILE_ID_ELEMENT_NAME = "profile-id";
    String CHANNEL_ELEMENT_NAME = "channel";
    String COUNT_ELEMENT_NAME = "count";
    String PERIOD_ELEMENT_NAME = "period";
    String DIRECTION_ELEMENT_NAME = "direction";

    void startStatistics(Date start, Date end);

    /** Invoked by the transformer upon the start of a new entry (row, line, etc.) of statistical data */
    void startEntry();

    void writeAccessPointIdentifier(String accessPointIdentifier);

    void writePeriod(String period);

    void writeDirection(String direction);

    void writeParticipantIdentifier(String participantId);

    void writeDocumentType(String documentType);

    void writeProfileId(String profileId);

    void writeChannel(String channel);

    void writeCount(int count);

    /** Completes the current statistics entry */
    void endEntry();

    void endStatistics();
}
