package eu.peppol.outbound.util;

import java.util.UUID;

public class Trace {

    private String identifier;

    private long timestamp;

    public static Trace of(String identifier) {
        return new Trace(identifier);
    }

    public static Trace generate() {
        return new Trace(UUID.randomUUID().toString());
    }

    private Trace(String identifier) {
        this.identifier = identifier;
        this.timestamp = System.currentTimeMillis();
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return String.format("[T|%s|%sms]", identifier, System.currentTimeMillis() - timestamp);
    }
}
