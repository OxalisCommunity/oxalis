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