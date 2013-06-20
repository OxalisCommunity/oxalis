package eu.peppol.start.identifier;

import java.io.Serializable;

/**
 * User: steinar
 * Date: 10.02.13
 * Time: 21:00
 */
public class AccessPointIdentifier implements Serializable {

    private final String accessPointIdentifierValue;

    public static final AccessPointIdentifier TEST = new AccessPointIdentifier("NO-TEST-AP");

    public AccessPointIdentifier(String accessPointIdentifierValue) {

        this.accessPointIdentifierValue = accessPointIdentifierValue;
    }


    @Override
    public String toString() {
        return accessPointIdentifierValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessPointIdentifier that = (AccessPointIdentifier) o;

        if (accessPointIdentifierValue != null ? !accessPointIdentifierValue.equals(that.accessPointIdentifierValue) : that.accessPointIdentifierValue != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return accessPointIdentifierValue != null ? accessPointIdentifierValue.hashCode() : 0;
    }
}
