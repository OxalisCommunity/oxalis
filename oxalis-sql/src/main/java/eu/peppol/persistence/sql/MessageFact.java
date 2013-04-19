package eu.peppol.persistence.sql;

/**
 * Represents an entry in the {@code message_fact}
 * table.
 *
 * @author steinar
 *         Date: 08.04.13
 *         Time: 10:56
 */
class MessageFact {
    public Integer apId;
    public Integer ppidId;
    public Integer documentTypeIdentifierId;
    public Integer channelId;
    public Integer profileId;
    public Integer timeId;
    public String direction;
    public Integer count;
}
