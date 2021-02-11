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

package network.oxalis.sniffer.document;

import com.google.common.collect.ImmutableMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;
import java.util.Map;

/**
 * Namespace resolver hard coded for UBL based documents only.
 *
 * @author steinar
 * @author thore
 * @author erlend
 */
public class HardCodedNamespaceResolver implements NamespaceContext {

    private static final Map<String, String> NAMESPACE_MAP = ImmutableMap.<String, String>builder()
            .put("xsi", "http://www.w3.org/2001/XMLSchema-instance")
            .put("cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
            .put("cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
            .put("ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2")
            .build();

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("No prefix provided!");
        }
        String uri = NAMESPACE_MAP.get(prefix);
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
