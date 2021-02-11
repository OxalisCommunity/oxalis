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

package network.oxalis.statistics.util;

import network.oxalis.statistics.api.StatisticsTransformer;
import network.oxalis.test.identifier.PeppolDocumentTypeIdAcronym;
import network.oxalis.test.identifier.PeppolProcessTypeIdAcronym;
import network.oxalis.api.model.Direction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import static org.testng.Assert.assertTrue;

/**
 * User: steinar
 * Date: 24.02.13
 * Time: 10:46
 */
public class StatisticsToXmlTransformerTest {

    private ByteArrayOutputStream byteArrayOutputStream;

    private StatisticsToXmlTransformer transformer;

    @BeforeMethod
    public void setUp() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        transformer = new StatisticsToXmlTransformer(byteArrayOutputStream);
    }

    @Test
    public void testWriteSampleStatisticsToXml() throws UnsupportedEncodingException {
        Date now = new Date();
        Date start = new Date(now.getTime() - 86400000L);
        Date end = new Date();

        transformer.startStatistics(start, end);
        transformer.startEntry();
        transformer.writeAccessPointIdentifier("AP-0001");
        transformer.writeParticipantIdentifier("9908:810017902");
        transformer.writeDirection(Direction.OUT.name());
        transformer.writePeriod("2013-01-T13");
        transformer.writeDocumentType(PeppolDocumentTypeIdAcronym.INVOICE.toString());
        transformer.writeProfileId(PeppolProcessTypeIdAcronym.INVOICE_ONLY.toString());
        transformer.writeChannel("SR-TEST");
        transformer.writeCount(10);
        transformer.endEntry();
        transformer.endStatistics();

        String s = byteArrayOutputStream.toString("UTF-8");

        assertTrue(s.contains(StatisticsTransformer.STATISTICS_DOCUMENT_START_ELEMENT_NAME));
        assertTrue(s.contains(StatisticsTransformer.ENTRY_START_ELEMENT_NAME));
        assertTrue(s.contains(StatisticsTransformer.ACCESS_POINT_ID_ELEMENT_NAME), "Missing " + StatisticsTransformer.PARTICIPANT_ID_ELEMENT_NAME + " element in result");
        assertTrue(s.contains(StatisticsTransformer.PARTICIPANT_ID_ELEMENT_NAME), "Missing " + StatisticsTransformer.PARTICIPANT_ID_ELEMENT_NAME);
        assertTrue(s.contains(StatisticsTransformer.CHANNEL_ELEMENT_NAME), "Missing " + StatisticsTransformer.CHANNEL_ELEMENT_NAME);
        assertTrue(s.contains(StatisticsTransformer.DOCUMENT_TYPE_ELEMENT_NAME));
        assertTrue(s.contains(StatisticsTransformer.PROFILE_ID_ELEMENT_NAME));
        assertTrue(s.contains(StatisticsTransformer.COUNT_ELEMENT_NAME));

        System.err.println(s);
    }

}
