/* Created by steinar on 20.05.12 at 13:00 */
package eu.peppol.start.identifier;

/**
 * Type safe value object holding a PEPPOL transaction id
 *
 * TODO: Consider refactoring as an enum type with all valid CEN BII Transaction data models.
 *
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class TransactionIdentifier {

    private final String transactionId;

    public TransactionIdentifier(String transactionId) {
        this.transactionId = transactionId;
    }


    public static TransactionIdentifier valueFor(String transactionId) {
        return new TransactionIdentifier(transactionId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionIdentifier that = (TransactionIdentifier) o;

        if (!transactionId.equals(that.transactionId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return transactionId.hashCode();
    }

    @Override
    public String toString() {
        return transactionId;
    }
}
