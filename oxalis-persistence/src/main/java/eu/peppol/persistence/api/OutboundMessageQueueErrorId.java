package eu.peppol.persistence.api;

/**
 * User: adam
 * Date: 16.03.13
 * Time: 15:53
 */
public class OutboundMessageQueueErrorId {
    private final Integer errorId;

    public OutboundMessageQueueErrorId(Integer queueId) {
        if (queueId == null || queueId <= 0) {
            throw new IllegalArgumentException("Invalid queue id");
        }
        this.errorId = queueId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OutboundMessageQueueErrorId that = (OutboundMessageQueueErrorId) o;

        if (errorId != null ? !errorId.equals(that.errorId) : that.errorId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return errorId != null ? errorId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return ""+ errorId;
    }

    public static OutboundMessageQueueErrorId valueOf(String queueId) {
        return new OutboundMessageQueueErrorId(Integer.valueOf(queueId));
    }

    public int toInt() {
        return errorId;
    }
}
