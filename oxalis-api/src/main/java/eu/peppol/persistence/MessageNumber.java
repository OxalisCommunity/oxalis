/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.persistence;

/**
 * Represents the unique identifier assigned to the message by our installation.
 *
 * The {@link eu.peppol.identifier.TransmissionId} is not unique as this is only assigned upon
 * actual transmission into the PEPPOL network.
 *
 * @author steinar
 *         Date: 22.10.2016
 *         Time: 17.52
 */
public final class MessageNumber {

    private Long messageNumber;

    public MessageNumber(Long messageNumber) {
        this.messageNumber = messageNumber;
    }

    public Long getValue() {
        return messageNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageNumber that = (MessageNumber) o;

        return messageNumber != null ? messageNumber.equals(that.messageNumber) : that.messageNumber == null;

    }

    @Override
    public int hashCode() {
        return messageNumber != null ? messageNumber.hashCode() : 0;
    }
}
