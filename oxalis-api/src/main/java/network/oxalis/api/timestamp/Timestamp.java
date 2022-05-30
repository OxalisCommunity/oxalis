/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.api.timestamp;

import network.oxalis.vefa.peppol.common.model.Receipt;

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
