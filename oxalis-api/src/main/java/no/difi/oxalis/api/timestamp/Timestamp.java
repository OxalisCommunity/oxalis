package no.difi.oxalis.api.timestamp;

import no.difi.vefa.peppol.common.model.Receipt;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

/**
 * Class used to hold a provided timestamp and a potential receipt as a result of fetching timestamp.
 *
 * @author erlend
 * @since 4.0.0
 */
public class Timestamp implements Serializable {

    private static final long serialVersionUID = -748252484013456945L;

    /**
     * Timestamp to be presented.
     */
    private final Date date;

    /**
     * Receipt to be presented
     */
    private final Optional<Receipt> receipt;

    /**
     * Constructor accepting a timestamp and potentially a receipt.
     *
     * @param date    Timestamp to be available.
     * @param receipt Receipt to be available.
     */
    public Timestamp(Date date, Receipt receipt) {
        this.date = date;
        this.receipt = Optional.ofNullable(receipt);
    }

    /**
     * Fetch timestamp.
     *
     * @return Timestamp.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Fetch receipt.
     *
     * @return Optional receipt.
     */
    public Optional<Receipt> getReceipt() {
        return receipt;
    }
}
