/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

/* Created by steinar on 20.05.12 at 13:00 */
package eu.peppol.identifier;

/**
 * Type safe value object holding a PEPPOL transaction id
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
