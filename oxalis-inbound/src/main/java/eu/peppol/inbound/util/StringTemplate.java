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
