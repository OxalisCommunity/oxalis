package eu.peppol.document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Performs a quick check of the document in order to determine whether it contains the SBDH or not.
 * This is done by simply checking the first 10 lines to see if they contain the the tag <code>StandardBusinessDocument</code>
 *
 * @author steinar
 *         Date: 06.11.13
 *         Time: 16:12
 */
public class DocumentSniffer {

    boolean sbdhDetected = false;

    public DocumentSniffer(InputStream resourceAsStream) {

        if (resourceAsStream.markSupported()) {
            resourceAsStream.mark(Integer.MAX_VALUE);
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
        String line;

        try {
            for (int i = 0; i < 50 && (line = bufferedReader.readLine()) != null; i++) {
                if (line.contains("<StandardBusinessDocument")) {
                    sbdhDetected = true;
                    break;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        if (resourceAsStream.markSupported()) {
            try {
                resourceAsStream.reset();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to reset the input stream: " + e.getMessage(), e);
            }
        }

    }

    public boolean isSbdhDetected() {
        return sbdhDetected;
    }

}
