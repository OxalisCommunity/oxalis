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

package eu.peppol.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: nigel
 * Date: Dec 13, 2011
 * Time: 4:21:49 PM
 */
public class JaxbContextCache {

    private static Map<Class, JAXBContext> cache = new HashMap<Class, JAXBContext>();

    public static synchronized JAXBContext getInstance(Class klasse) throws JAXBException {

        if (!cache.containsKey(klasse)) {
            cache.put(klasse, JAXBContext.newInstance(klasse));
        }

        return cache.get(klasse);
    }
}
