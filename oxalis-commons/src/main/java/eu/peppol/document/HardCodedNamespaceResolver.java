package eu.peppol.document;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author steinar
 *         Date: 05.11.13
 *         Time: 16:38
 */
public class HardCodedNamespaceResolver implements NamespaceContext {

    static String[][] namespaces = {

            {XMLConstants.DEFAULT_NS_PREFIX, "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"},
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

    /**
     * This method returns the uri for all prefixes needed. Wherever possible
     * it uses XMLConstants.
     *
     * @param prefix
     * @return uri
     */
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

    public String getPrefix(String namespaceURI) {
        // Not needed in this context.
        return null;
    }

    public Iterator getPrefixes(String namespaceURI) {
        // Not needed in this context.
        return null;
    }

}