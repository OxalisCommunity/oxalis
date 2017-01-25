package no.difi.oxalis.commons.filesystem;

public class FileUtils {

    public static String filterString(String s) {
        return s.replaceAll("[^a-zA-Z0-9.\\-]", "_");
    }
}
