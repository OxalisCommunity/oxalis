package eu.peppol.persistence.api;

/**
 * User: adam
 * Date: 16.03.13
 * Time: 15:53
 */
public class OutboundMessageQueueId {
    private final Integer queueId;

    public OutboundMessageQueueId(Integer queueId) {
        if (queueId == null || queueId <= 0) {
            throw new IllegalArgumentException("Invalid queue id");
        }
        this.queueId = queueId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OutboundMessageQueueId that = (OutboundMessageQueueId) o;

        if (queueId != null ? !queueId.equals(that.queueId) : that.queueId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return queueId != null ? queueId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return ""+queueId;
    }

    public static OutboundMessageQueueId valueOf(String queueId) {
        return new OutboundMessageQueueId(Integer.valueOf(queueId));
    }

    public int toInt() {
        return queueId;
    }
}
