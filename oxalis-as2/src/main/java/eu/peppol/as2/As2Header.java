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


    AS2_VERSION("as2-version"),
    AS2_FROM("as2-from"),
    AS2_TO("as2-to"),
    SUBJECT("subject"),
    MESSAGE_ID("message-id"),
    DATE("date"),
    DISPOSITION_NOTIFICATION_TO("disposition-notification-to"),
    DISPOSITION_NOTIFICATION_OPTIONS("disposition-notification-options"),
    RECEIPT_DELIVERY_OPTION("receipt-delivery-option"), SERVER("server");

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
