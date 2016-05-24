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

package eu.peppol.inbound.util;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author $Author$ (of last change)
 *         Created by
 *         User: steinar
 *         Date: 13.11.11
 *         Time: 22:23
 */
public class StringTemplateTest {
    
    @Test
    public void replace() {
        Map<String, String> m = new HashMap<String, String>();
        m.put("ChannelID", "C1");
        m.put("RecipientID", "9908:976098897");
        m.put("SenderID", "9908:123456789");

        String s = StringTemplate.interpolate("/tmp/inbox/${RecipientID}/${ChannelID}/${SenderID}/", m);
        assertEquals("/tmp/inbox/9908_976098897/C1/9908_123456789/", s);

        // Verifies that missing variables, which might cause the string "//" to be inserted, are removed
        s = StringTemplate.interpolate("/tmp/inbox/${RecipientID}//${SenderID}/", m);
        assertEquals("/tmp/inbox/9908_976098897/9908_123456789/", s);
    }
}
