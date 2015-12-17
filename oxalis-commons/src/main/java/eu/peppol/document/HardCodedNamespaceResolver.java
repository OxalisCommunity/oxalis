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

package eu.peppol.document;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Namespace resolver hard coded for UBL based documents only.
 *
 * @author steinar
 * @author thore
 */
public class HardCodedNamespaceResolver implements NamespaceContext {

    static String[][] namespaces = {
            {"xsi", "http://www.w3.org/2001/XMLSchema-instance"},
            {"cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"},
            {"cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"},
            {"ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2"}
    };

    private final HashMap<String,String> namespaceMap;

    public HardCodedNamespaceResolver() {
        // Shoves the name space declarations into a HashMap to make life easier
        namespaceMap = new HashMap<String, String>();
        for (String[] entry : namespaces) {
            namespaceMap.put(entry[0], entry[1]);
        }
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("No prefix provided!");
        }
        String uri = namespaceMap.get(prefix);
        if (uri == null) {
            return XMLConstants.NULL_NS_URI;
        } else {
            return uri;
        }
    }

    @Override
    public String getPrefix(String namespaceURI) {
        // Not needed in this context.
        return null;
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        // Not needed in this context.
        return null;
    }

}