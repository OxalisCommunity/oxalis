package eu.peppol.identifier;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents the unique identification of transmission. I.e. a given message, with a Message ID, may be
 * transmitted several times, having a unique transmission ID every time.
 *
 * @author steinar
 *         Date: 08.11.13
 *         Time: 09:48
 */
public class TransmissionId implements Serializable {

    private static final long serialVersionUID = 4278193961456528215L;

    private String value;

    public TransmissionId() {
        value = UUID.randomUUID().toString();
    }

    public TransmissionId(String value) {
        if (value == null) {
            throw new IllegalArgumentException("TransmissionId as a UUID represented in text required");
        }

        this.value = value;
    }

    public TransmissionId(UUID uuid) {
        this.value = uuid.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransmissionId that = (TransmissionId) o;

        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
