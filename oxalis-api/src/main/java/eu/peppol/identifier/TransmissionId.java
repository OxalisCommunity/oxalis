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


    private String value;

    /** Generates a unique transmission identifier based upon UUID */
    public TransmissionId() {
        value = UUID.randomUUID().toString();
    }

    public TransmissionId(String value) {
        this.value = value;
    }

    public TransmissionId(UUID uuid) {
        this.value = uuid.toString();
    }

}
