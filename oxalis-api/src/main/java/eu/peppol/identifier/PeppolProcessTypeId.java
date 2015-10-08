/* Created by steinar on 23.05.12 at 23:09 */
package eu.peppol.identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Represents a type safe PEPPOL Process type identifier.
 *
 * @see "Policy for use of Identifiers, section 5"
 *
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class PeppolProcessTypeId implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(PeppolProcessTypeId.class);

    // See Policy 15 and policy 17
    private static final String scheme = "cenbii-procid-ubl";

    private final String processTypeIdentifer;

    public PeppolProcessTypeId(String processTypeIdentifer) {
        if (!processTypeIdentifer.startsWith("urn:")) {
            // TODO Change to exception when suitable.
            log.info("PEPPOL process type identifier should start with \"urn\"");
        }

        this.processTypeIdentifer = processTypeIdentifer;
    }

    public String getScheme() {
        return scheme;
    }

    @Override
    public String toString() {
        return processTypeIdentifer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeppolProcessTypeId that = (PeppolProcessTypeId) o;

        if (processTypeIdentifer != null ? !processTypeIdentifer.equals(that.processTypeIdentifer) : that.processTypeIdentifer != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return processTypeIdentifer != null ? processTypeIdentifer.hashCode() : 0;
    }


    public static PeppolProcessTypeId valueOf(String processTypeIdentifer) {
        return new PeppolProcessTypeId(processTypeIdentifer);
    }
}
