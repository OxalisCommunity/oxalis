package eu.peppol.as2;

import eu.peppol.security.CommonName;

import javax.security.auth.x500.X500Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the unique identification of an AS2 system.
 *
 * @author steinar
 *         Date: 09.10.13
 *         Time: 10:22
 */
public class As2SystemIdentifier {

    // No Quotes, no back slashes and a maximum of 128 characters
    public static final String AS2_NAME = "\\A[^\"\\\\]{1,128}\\Z";

    public static Pattern as2NamePattern = Pattern.compile(AS2_NAME);

    private final String as2Name;

    public As2SystemIdentifier(String as2Name) throws InvalidAs2SystemIdentifierException {
        this.as2Name = as2Name;
        Matcher matcher = as2NamePattern.matcher(as2Name);
        if (!matcher.matches()) {
            throw new InvalidAs2SystemIdentifierException(as2Name );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        As2SystemIdentifier that = (As2SystemIdentifier) o;

        if (!as2Name.equals(that.as2Name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return as2Name.hashCode();
    }


    @Override
    public String toString() {
        return as2Name;
    }

}
