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

import java.util.Map;

/**
 * @author $Author$ (of last change)
 *         Created by
 *         User: steinar
 *         Date: 13.11.11
 *         Time: 22:23
 */
public class StringTemplate {
    public static String interpolate(String s, Map<String, String> m) {

        String result = s;

        for (Map.Entry<String, String> entry : m.entrySet()) {

            String regex = "\\$\\{" + entry.getKey() + "\\}";
            
            // Replaces all characters which are illegal or problematic in filenames with _
            String replacement = entry.getValue().replaceAll("[/:]","_");
            result = result.replaceAll(regex, replacement);
        }

        return result.replaceAll("//","/");
    }
}
