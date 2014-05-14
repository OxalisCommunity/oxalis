/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
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
