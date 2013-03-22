package eu.peppol.statistics.repository;

import eu.peppol.start.identifier.AccessPointIdentifier;

import javax.activation.MimeType;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author steinar
 *         Date: 22.03.13
 *         Time: 10:01
 */
class FilenameGenerator {

    public static final String META_DATA_SUFFIX = ".meta.txt";

    static Pattern filenamePattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{2}_\\d{2}_\\d{2}_\\d{3})\\..*$");

    public static String filenameFromTimeStamp(Date date) {
        return String.format("%1$tFT%1$tH_%1$tM_%1$tS_%1$tL", date);
    }

    public static File dataFile(File rootDirectory, AccessPointIdentifier accessPointIdentifier, MimeType mimeType, Date ts) {
        String fileName = filenameFromTimeStamp(ts);

        return new File(new File(rootDirectory, accessPointIdentifier.toString()), fileName + filenameExtension(mimeType, fileName));
    }

    public static File metadataFile(File rootDirectory, AccessPointIdentifier accessPointIdentifier, MimeType mimeType, Date ts) {
        String fileName = filenameFromTimeStamp(ts);

        return new File(new File(rootDirectory, accessPointIdentifier.toString()), fileName + filenameExtension(mimeType, fileName) + META_DATA_SUFFIX);
    }

    static String filenameExtension(MimeType mimeType, String fileName) {
        String extension = null;

        if (mimeType.getPrimaryType().equals("text")) {
            if (mimeType.getSubType().equals("plain")) {
                extension = ".txt";
            } else {
                extension = "." + mimeType.getSubType();
            }
        } else if (mimeType.getPrimaryType().equals("application")) {
            extension = "." + mimeType.getSubType();
        }
        return extension;
    }


    public static String getFilenameWithoutExtension(File f) {
        Matcher m = filenamePattern.matcher(f.getAbsolutePath());

        if (!m.find()) {
            throw new IllegalArgumentException("File " + f.getAbsolutePath() + " does not match our file naming standards");
        }
        String name = m.group(1);
        return name;
    }

    public static boolean isMetadataFile(File file) {
        return file.getAbsolutePath().endsWith(META_DATA_SUFFIX);
    }


    /**
     * <pre>
     *  /tmp/data/x/y/z/filename.txt -> /tmp/archive/x/y/z/filename.txt
     * </pre>
     * Old directory is <code>/tmp/data</code>, to be renamed to <code>/tmp/archive</code> retaining everything after the
     * initial prefix.
     *
     */
    public static File filenameForMoveToAnotherDirectory(File srcRootDir, File destRootDir, File file) {
        String newFilename = file.getAbsolutePath().replace(srcRootDir.getAbsolutePath(), destRootDir.getAbsolutePath());
        return new File(newFilename);
    }
}
