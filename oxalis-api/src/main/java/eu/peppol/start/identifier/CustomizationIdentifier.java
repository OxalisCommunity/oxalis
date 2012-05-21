/* Created by steinar on 20.05.12 at 12:07 */
package eu.peppol.start.identifier;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a PEPPOL Customization Identifier contained within a PEPPOL  Document Identifier, for which the
 * pattern is
 * <pre>
 *     &lt;transactionId>:#&lt;extensionId>[#&lt;extensionId>]
 * </pre>
 * I.e. a string followed by ":#" followed by another string followed by an optional string starting with "#"
 * and followed by another string.

 * @author Steinar Overbeck Cook steinar@sendregning.no
 *
 * @see "PEPPOL Policy for us of Identifiers v2.2"
 */
public class CustomizationIdentifier {


    /**
     */
    static Pattern customizationIdentifierPattern = Pattern.compile("(.*):#([^#]*)(?:#(.*))?");
    private final TransactionIdentifier transactionIdentifier;
    private final ExtensionIdentifier extensionIdentifier1;
    private final ExtensionIdentifier extensionIdentifier2;

    private ExtensionIdentifier[] extensionIdentifiers;

    public CustomizationIdentifier(TransactionIdentifier transactionIdentifier, ExtensionIdentifier extensionIdentifier1, ExtensionIdentifier extensionIdentifier2) {

        this.transactionIdentifier = transactionIdentifier;
        this.extensionIdentifier1 = extensionIdentifier1;
        this.extensionIdentifier2 = extensionIdentifier2;

        if (extensionIdentifier2 != null){
            extensionIdentifiers = new ExtensionIdentifier[]{extensionIdentifier1, extensionIdentifier2};
        } else
            extensionIdentifiers = new ExtensionIdentifier[]{extensionIdentifier1};
    }

    public CustomizationIdentifier(TransactionIdentifier transactionIdentifier, ExtensionIdentifier extensionIdentifier) {
        this(transactionIdentifier, extensionIdentifier, null);
    }


    static CustomizationIdentifier valueOf(String s) {

        CustomizationIdentifier result = null;

        Matcher matcher = customizationIdentifierPattern.matcher(s);
        if (!matcher.find()){
            throw new IllegalArgumentException(s + " not recognized as customization identifier");
        }

        String transactionId = matcher.group(1);
        TransactionIdentifier transactionIdentifier = TransactionIdentifier.valueFor(transactionId);

        String extensionIdValue = matcher.group(2);
        ExtensionIdentifier extensionIdentifier1 = ExtensionIdentifier.valueFor(extensionIdValue);

        // Is there a second extension within the customization string?
        if (matcher.group(3) != null) {
            ExtensionIdentifier extensionIdentifier2 = ExtensionIdentifier.valueFor(matcher.group(3));

            result = new CustomizationIdentifier(transactionIdentifier, extensionIdentifier1, extensionIdentifier2);

        } else {
            result = new CustomizationIdentifier(transactionIdentifier, extensionIdentifier1);
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomizationIdentifier that = (CustomizationIdentifier) o;

        if (transactionIdentifier != null ? !transactionIdentifier.equals(that.transactionIdentifier) : that.transactionIdentifier != null)
            return false;
        if (extensionIdentifier1 != null ? !extensionIdentifier1.equals(that.extensionIdentifier1) : that.extensionIdentifier1 != null)
            return false;
        if (extensionIdentifier2 != null ? !extensionIdentifier2.equals(that.extensionIdentifier2) : that.extensionIdentifier2 != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = transactionIdentifier != null ? transactionIdentifier.hashCode() : 0;
        result = 31 * result + (extensionIdentifier1 != null ? extensionIdentifier1.hashCode() : 0);
        result = 31 * result + (extensionIdentifier2 != null ? extensionIdentifier2.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(transactionIdentifier).append(":#");
        sb.append(extensionIdentifier1);
        if (extensionIdentifier2 != null) {
            sb.append("#").append(extensionIdentifier2);
        }
        return sb.toString();
    }

    public TransactionIdentifier getTransactionIdentifier() {
        return transactionIdentifier;
    }

    public ExtensionIdentifier getFirstExtensionIdentifier() {
        return extensionIdentifier1;
    }

    public ExtensionIdentifier getSecondExtensionIdentifier() {
        return extensionIdentifier2;
    }
}
