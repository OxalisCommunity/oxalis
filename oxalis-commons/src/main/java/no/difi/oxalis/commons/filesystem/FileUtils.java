package no.difi.oxalis.commons.filesystem;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class FileUtils {

    public static String filterString(String s) {
        return s.replaceAll("[^a-zA-Z0-9.\\-]", "_");
    }

    public static URL toUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
