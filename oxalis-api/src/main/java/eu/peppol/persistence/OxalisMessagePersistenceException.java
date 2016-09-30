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

package eu.peppol.persistence;

import eu.peppol.PeppolMessageMetaData;

/**
 * @author steinar
 *         Date: 09.08.13
 *         Time: 14:10
 */
public class OxalisMessagePersistenceException extends Exception {

    private static final String MSG = "Unable to persist XML document for ";
    private final PeppolMessageMetaData peppolMessageMetaData;

    public OxalisMessagePersistenceException(String unknownReceipientMsg, PeppolMessageMetaData peppolMessageHeader) {
        super(unknownReceipientMsg);

        peppolMessageMetaData = peppolMessageHeader;
    }

    public OxalisMessagePersistenceException(PeppolMessageMetaData peppolMessageMetaData) {
        this(MSG + peppolMessageMetaData, peppolMessageMetaData);
    }

    public OxalisMessagePersistenceException(PeppolMessageMetaData peppolMessageMetaData, Throwable cause) {
        super(MSG + peppolMessageMetaData, cause);

        this.peppolMessageMetaData = peppolMessageMetaData;
    }
}
