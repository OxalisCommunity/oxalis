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
