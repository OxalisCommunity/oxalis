package eu.peppol.outbound.util;

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
