package eu.peppol.document;

import eu.peppol.xml.XmlUtils;

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
public class DocumentSnifferSimpleImpl implements DocumentSniffer {

    private static final String SBDH_NS = "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader";

    boolean sbdhDetected = false;

    public DocumentSnifferSimpleImpl(InputStream resourceAsStream) {

        if (resourceAsStream.markSupported()) {
            resourceAsStream.mark(Integer.MAX_VALUE);
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));

        try {
            char[] content = new char[512];
            bufferedReader.read(content);

            sbdhDetected = SBDH_NS.equals(XmlUtils.extractRootNamespace(new String(content)));
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

    @Override
    public boolean isSbdhDetected() {
        return sbdhDetected;
    }

}
