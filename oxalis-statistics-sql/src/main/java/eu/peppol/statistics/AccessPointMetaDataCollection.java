package eu.peppol.statistics;

import eu.peppol.identifier.AccessPointIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds meta data for PEPPOL access points. See {@link #loadAccessPointMetaData(java.io.File)} for a description of
 * the file format expected when loading from an external file.
 *
 * @author steinar
 *         Date: 07.03.13
 *         Time: 14:10
 */
public class AccessPointMetaDataCollection {

    public static final int META_DATA_MINIMUM_COLUMN_COUNT = 5;
    public static final String ACCESS_POINTS_CSV = "access-points.csv";
    List<AccessPointMetaData> accessPointMetaDataList = new ArrayList<AccessPointMetaData>();

    private static final Logger log = LoggerFactory.getLogger(AccessPointMetaDataCollection.class);

    public AccessPointMetaDataCollection() {
    }

    public AccessPointMetaDataCollection(File file) {
        if (file.exists() && file.isFile() && file.canRead()) {
            accessPointMetaDataList = loadAccessPointMetaData(file);
        }
    }

    public AccessPointMetaDataCollection(InputStream inputStream) {
        accessPointMetaDataList = loadAccessPointMetaData(inputStream);
    }

    public static AccessPointMetaDataCollection loadIncludedListOfAccessPoints() {


        InputStream resourceAsStream = AccessPointMetaDataCollection.class.getClassLoader().getResourceAsStream(ACCESS_POINTS_CSV);
        if (resourceAsStream == null) {
            throw new IllegalStateException("Unable to load " + ACCESS_POINTS_CSV + " from class path");
        }

        AccessPointMetaDataCollection accessPointMetaDataCollection = new AccessPointMetaDataCollection(resourceAsStream);
        return accessPointMetaDataCollection;
    }

    /**
     * Provides an unmodifiable list of all the access point meta data entries.
     *
     * @return unmodifiable list of AP meta data
     */
    public List<AccessPointMetaData> getAccessPointMetaDataList() {
        return Collections.unmodifiableList(accessPointMetaDataList);
    }


    List<AccessPointMetaData> loadAccessPointMetaData(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return loadAccessPointMetaData(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Unable to open " + file.getAbsolutePath() + "; " + e.getMessage(), e);

        }
    }

    /**
     * Loads a CSV file containing the following information, separated by either ';' or TAB:
     * <ol>
     * <li>Organisation name</li>
     * <li>Organisation number</li>
     * <li>Description of access point</li>
     * <li>The URL for the START end point</li>
     * </ol>
     *
     * @param inputStream input stream connected to the file holding the CVS file, will be read using UTF-8
     * @return
     */
    List<AccessPointMetaData> loadAccessPointMetaData(InputStream inputStream) {

        List<AccessPointMetaData> result = new ArrayList<AccessPointMetaData>();

        LineNumberReader lineNumberReader = null;

        try {
            lineNumberReader = new LineNumberReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));

            String line;
            while ((line = lineNumberReader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                if (line.trim().length() == 0) {
                    continue;
                }

                AccessPointMetaData accessPointMetaData = processLine(lineNumberReader.getLineNumber(), line);
                result.add(accessPointMetaData);
            }

        } catch (IOException e) {
            throw new IllegalStateException("I/O error at line " + lineNumberReader.getLineNumber(), e);
        } finally {
            if (lineNumberReader != null) {
                try {
                    lineNumberReader.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to close inputstream ; " + e.getMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Parses a single line into an AccessPointMetaData object
     *
     * @param lineNumber the current line number
     * @param line       the textual contents of line to be parsed
     * @return an AccessPointMetaData object.
     */
    AccessPointMetaData processLine(int lineNumber, String line) {
        String[] strings = line.split("[;\\t]");
        if (strings.length < META_DATA_MINIMUM_COLUMN_COUNT) {
            log.warn("Error at line " + lineNumber + ", expected " + META_DATA_MINIMUM_COLUMN_COUNT + " fields found " + strings.length + ": " + line);
        }

        URL url = null;
        String urlAsString = strings[4];
        try {
            url = new URL(urlAsString);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid url at line " + lineNumber + ": " + urlAsString);
        }

        URL statisticsUrl = null;
        if (strings.length >= 6) {
            String statisticsUrlAsString = strings[5];
            try {
                statisticsUrl = new URL(statisticsUrlAsString);
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Statistics URL on line " + lineNumber + " is invalid: " + statisticsUrlAsString);
            }
        }


        return new AccessPointMetaData(new AccessPointIdentifier(strings[0]), strings[1], strings[2], strings[3], url, statisticsUrl);
    }
}
