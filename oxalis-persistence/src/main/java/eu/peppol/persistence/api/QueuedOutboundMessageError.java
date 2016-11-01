package eu.peppol.persistence.api;

import java.util.Date;

/**
 * User: adam
 * Date: 16.03.13
 * Time: 16:03
 */
public class QueuedOutboundMessageError {
    private final OutboundMessageQueueErrorId errorId;
    private final OutboundMessageQueueId outboundQueueId;
    private final String message;
    private final String details;
    private final String stacktrace;
    private final Date createDT;
    // retrieved by join
    private final MessageNumber messageNumber;
    private final String invoiceNumber;


    // for ones fetched
    public QueuedOutboundMessageError(OutboundMessageQueueErrorId errorID, OutboundMessageQueueId outboundQueueID, MessageNumber messageNumber, String message, String details, String stacktrace, Date createDT, String invoiceNumber) {
        this.errorId = errorID;
        this.outboundQueueId = outboundQueueID;
        this.messageNumber = messageNumber;
        this.message = message;
        this.details = details;
        this.stacktrace = stacktrace;
        this.createDT = createDT;
        this.invoiceNumber = invoiceNumber;
    }

    // for ones created in java
    public QueuedOutboundMessageError(OutboundMessageQueueId outboundQueueID, String details, String message, String stacktrace) {
        this.messageNumber = null;
        this.invoiceNumber = null;
        this.createDT = null;
        this.errorId = null;
        this.outboundQueueId = outboundQueueID;
        this.message = message;
        this.details = details;
        this.stacktrace = stacktrace;

    }

    public OutboundMessageQueueErrorId getErrorId() {
        return errorId;
    }

    public OutboundMessageQueueId getOutboundQueueId() {
        return outboundQueueId;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public Date getCreateDT() {
        return createDT;
    }

    public MessageNumber getMessageNumber() {
        return messageNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueuedOutboundMessageError that = (QueuedOutboundMessageError) o;

        if (createDT != null ? !createDT.equals(that.createDT) : that.createDT != null) return false;
        if (details != null ? !details.equals(that.details) : that.details != null) return false;
        if (errorId != null ? !errorId.equals(that.errorId) : that.errorId != null) return false;
        if (invoiceNumber != null ? !invoiceNumber.equals(that.invoiceNumber) : that.invoiceNumber != null)
            return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (messageNumber != null ? !messageNumber.equals(that.messageNumber) : that.messageNumber != null)
            return false;
        if (outboundQueueId != null ? !outboundQueueId.equals(that.outboundQueueId) : that.outboundQueueId != null)
            return false;
        if (stacktrace != null ? !stacktrace.equals(that.stacktrace) : that.stacktrace != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = errorId != null ? errorId.hashCode() : 0;
        result = 31 * result + (outboundQueueId != null ? outboundQueueId.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        result = 31 * result + (stacktrace != null ? stacktrace.hashCode() : 0);
        result = 31 * result + (createDT != null ? createDT.hashCode() : 0);
        result = 31 * result + (messageNumber != null ? messageNumber.hashCode() : 0);
        result = 31 * result + (invoiceNumber != null ? invoiceNumber.hashCode() : 0);
        return result;
    }

    public String getInvoiceNumber() {

        return invoiceNumber;
    }

    @Override
    public String toString() {
        return "QueuedOutboundMessageError{" +
                "errorId=" + errorId +
                ", outboundQueueId=" + outboundQueueId +
                ", message='" + message + '\'' +
                ", details='" + details + '\'' +
                ", stacktrace='" + stacktrace + '\'' +
                ", createDT=" + createDT +
                ", messageNumber=" + messageNumber +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                '}';
    }
}
