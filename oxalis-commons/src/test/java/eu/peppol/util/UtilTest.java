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

/* Created by steinar on 18.05.12 at 13:55 */
package eu.peppol.util;

import org.testng.annotations.Test;

import javax.mail.util.SharedByteArrayInputStream;
import java.io.InputStream;

import static org.testng.Assert.*;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class UtilTest {

    @Test
    public void testCalculateMD5() throws Exception {
        String hash = Util.calculateMD5("9908:810017902");

        assertEquals(hash, "ddc207601e442e1b751e5655d39371cd");
    }


    /**
     * Experiments with byte arrays in order to verify that our understanding of the API is correct
     */
    @Test
    public void duplicateInputStream() throws Exception {

        InputStream inputStream = UtilTest.class.getClassLoader().getResourceAsStream("peppol-bis-invoice-sbdh.xml");
        assertNotNull(inputStream);

        byte[] bytes = Util.intoBuffer(inputStream, 5L * 1024 * 1024);
        String s = new String(bytes);
        assertTrue(s.contains("</StandardBusinessDocument>"));


        SharedByteArrayInputStream sharedByteArrayInputStream = new SharedByteArrayInputStream(bytes);
        InputStream inputStream1 = sharedByteArrayInputStream.newStream(0, -1);

        byte[] b2 = Util.intoBuffer(inputStream1, 5L * 1024 * 1024);
        assertEquals(bytes.length, b2.length);
    }


}
