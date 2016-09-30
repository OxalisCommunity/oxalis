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

package eu.peppol.identifier;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents the unique identification of transmission. I.e. a given message, with a Message ID, may be
 * transmitted several times, having a unique transmission ID every time.
 *
 * @author steinar
 *         Date: 08.11.13
 *         Time: 09:48
 */
public class TransmissionId implements Serializable {

    private static final long serialVersionUID = 4278193961456528215L;

    private String value;

    public TransmissionId() {
        value = UUID.randomUUID().toString();
    }

    public TransmissionId(String value) {
        if (value == null) {
            throw new IllegalArgumentException("TransmissionId as a UUID represented in text required");
        }

        this.value = value;
    }

    public TransmissionId(UUID uuid) {
        this.value = uuid.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransmissionId that = (TransmissionId) o;

        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
