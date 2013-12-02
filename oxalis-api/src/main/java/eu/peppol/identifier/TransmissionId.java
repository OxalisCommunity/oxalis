package eu.peppol.identifier;

import java.util.UUID;

/**
 * Represents the unique identification of transmission. I.e. a given message, with a Message ID, may be
 * transmitted several times, having a unique transmission ID every time.
 *
 * @author steinar
 *         Date: 08.11.13
 *         Time: 09:48
 */
public class TransmissionId {


    private  UUID uuid;

    public TransmissionId() {
        uuid = UUID.randomUUID();
    }

    // TODO: refactor TransmissionId to accept arbitrary strings rather than UUIDs
    public TransmissionId(String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("TransmissionId as a UUID represented in text required");
        }
        try {
            UUID.fromString(uuid);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid UUID supplied as transmission id");
        }
    }

    public TransmissionId(UUID uuid) {

        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransmissionId that = (TransmissionId) o;

        if (!uuid.equals(that.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return uuid.toString();
    }
}
