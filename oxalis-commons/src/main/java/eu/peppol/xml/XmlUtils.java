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

package eu.peppol.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Part of vefa-peppol.
 * https://github.com/difi/vefa-peppol
 */
public class XmlUtils {

    private static Logger logger = LoggerFactory.getLogger(XmlUtils.class);

    private static final Pattern rootTagPattern = Pattern.compile("<(\\w*:{0,1}[^<?]*)>", Pattern.MULTILINE);
    private static final Pattern namespacePattern = Pattern.compile("xmlns:{0,1}([a-z0-9]*)\\w*=\\w*\"(.+?)\"", Pattern.MULTILINE);

    public static String extractRootNamespace(String xmlContent) {
        Matcher matcher = rootTagPattern.matcher(xmlContent);
        if (matcher.find()) {
            String rootElement = matcher.group(1).trim();
            logger.debug("Root element: {}", rootElement);
            String rootNs = rootElement.split(" ", 2)[0].contains(":") ? rootElement.substring(0, rootElement.indexOf(":")) : "";
            logger.debug("Namespace: {}", rootNs);

            Matcher nsMatcher = namespacePattern.matcher(rootElement);
            while (nsMatcher.find()) {
                logger.debug(nsMatcher.group(0));

                if (nsMatcher.group(1).equals(rootNs)) {
                    return nsMatcher.group(2);
                }
            }
        }

        return null;
    }

    XmlUtils() {

    }
}
