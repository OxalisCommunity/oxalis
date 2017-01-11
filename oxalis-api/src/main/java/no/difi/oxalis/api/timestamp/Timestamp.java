package no.difi.oxalis.api.timestamp;

import no.difi.vefa.peppol.common.model.Receipt;

import java.io.Serializable;
import java.util.Date;

public class Timestamp implements Serializable {

    private static final long serialVersionUID = -748252484013456945L;

    private Date date;

    private Receipt receipt;

    public Timestamp(Date date, Receipt receipt) {
        this.date = date;
        this.receipt = receipt;
    }

    public Date getDate() {
        return date;
    }

    public Receipt getReceipt() {
        return receipt;
    }
}
