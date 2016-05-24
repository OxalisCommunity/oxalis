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

package eu.peppol.as2;

/**
 * Defines the AS2-Specific HTTP Headers according to RFC4130 section 6.
 *
 * @author steinar
 *         Date: 07.10.13
 *         Time: 22:35
 */
public enum As2Header {


    AS2_VERSION("AS2-Version"),
    AS2_FROM("AS2-From"),
    AS2_TO("AS2-To"),
    SUBJECT("Subject"),
    MESSAGE_ID("Message-ID"),
    DATE("Date"),
    DISPOSITION_NOTIFICATION_TO("Disposition-Notification-To"),
    DISPOSITION_NOTIFICATION_OPTIONS("Disposition-Notification-Options"),
    RECEIPT_DELIVERY_OPTION("Receipt-Delivery-Option"),
    SERVER("Server");

    // PEPPOL Transport Infrastructure AS2 Profile specifies AS2 version 1.0
    public static final String VERSION = "1.0";

    private final String httpHeaderName;

    As2Header(String httpHeaderName) {
        this.httpHeaderName = httpHeaderName;
    }

    public String getHttpHeaderName() {
        return httpHeaderName;
    }
}
