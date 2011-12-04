package eu.peppol.start.identifier;

/**
 * Represents the identifiers used in the START SOAP Headers, which are used
 * for routing, thus preventing the need to open the XML messages.
 * 
 * @author Steinar Overbeck Cook
 *         Created by
 *         User: steinar
 *         Date: 29.11.11
 *         Time: 13:44
 */
public enum IdentifierName {

     MESSAGE_ID     ("MessageIdentifier"),
     CHANNEL_ID     ("ChannelIdentifier"),
     RECIPIENT_ID   ("RecipientIdentifier"),
     SENDER_ID      ("SenderIdentifier"),
     DOCUMENT_ID    ("DocumentIdentifier"),
     PROCESS_ID     ("ProcessIdentifier"),
     SCHEME         ("scheme")
    ;

    String value;

    private IdentifierName(String value){
        this.value = value;
    }

    public String stringValue() {
        return value;
    }

    public static IdentifierName valueOfIdentifier(String stringValue) {
        for (IdentifierName id : IdentifierName.values()) {
          if (id.value.equals(stringValue))
              return id;
        }

        throw new IllegalArgumentException("Unknown identifer: " + stringValue);
    }
}
