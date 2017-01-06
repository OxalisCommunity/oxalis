/*
 * Copyright (c) 2010 - 2017 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.persistence.queue;

/**
 * User: adam
 * Date: 16.03.13
 * Time: 15:53
 */
public class OutboundMessageQueueErrorId {
    private final Integer errorId;

    public OutboundMessageQueueErrorId(Integer queueId) {
        if (queueId == null || queueId <= 0) {
            throw new IllegalArgumentException("Invalid queue id");
        }
        this.errorId = queueId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OutboundMessageQueueErrorId that = (OutboundMessageQueueErrorId) o;

        if (errorId != null ? !errorId.equals(that.errorId) : that.errorId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return errorId != null ? errorId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return ""+ errorId;
    }

    public static OutboundMessageQueueErrorId valueOf(String queueId) {
        return new OutboundMessageQueueErrorId(Integer.valueOf(queueId));
    }

    public int toInt() {
        return errorId;
    }
}
