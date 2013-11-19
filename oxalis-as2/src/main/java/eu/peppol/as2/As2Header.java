package eu.peppol.as2;

/**
 * Defines the AS2-Specific HTTP Headers according to RFC4130 section 6.
 *
 *
 * @author steinar
 *         Date: 07.10.13
 *         Time: 22:35
 */
public enum As2Header {


    AS2_VERSION("AS2-Version"),
    AS2_FROM("AS2-From"),
    AS2_TO("AS2-To"),
    SUBJECT("Subject"),
    MESSAGE_ID("Message-ID"),
    DATE("Date"),
    DISPOSITION_NOTIFICATION_TO("Disposition-Notification-To"),
    DISPOSITION_NOTIFICATION_OPTIONS("Disposition-Notification-Options"),
    RECEIPT_DELIVERY_OPTION("Receipt-Delivery-Option"),
    SERVER("Server");

    // Which version of AS2 do we support?
    public static final String VERSION = "1.0";

    private final String httpHeaderName;

    As2Header(String httpHeaderName) {
        this.httpHeaderName = httpHeaderName;
    }

    public String getHttpHeaderName() {
        return httpHeaderName;
    }
}
