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

package network.oxalis.as2.code;

/**
 * Defines the AS2-Specific HTTP Headers according to RFC4130 section 6.
 *
 * @author steinar
 * @author erlend
 */
public class As2Header {

    public static final String MESSAGE_ID = "Message-Id";

    public static final String MIME_VERSION = "MIME-Version";

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String AS2_VERSION = "AS2-Version";

    public static final String AS2_FROM = "AS2-From";

    public static final String AS2_TO = "AS2-To";

    public static final String SUBJECT = "Subject";

    public static final String DATE = "Date";

    public static final String DISPOSITION_NOTIFICATION_TO = "Disposition-Notification-To";

    public static final String DISPOSITION_NOTIFICATION_OPTIONS = "Disposition-Notification-Options";

    public static final String RECEIPT_DELIVERY_OPTION = "Receipt-Delivery-Option";

    public static final String SERVER = "Server";

    // PEPPOL Transport Infrastructure AS2 Profile specifies AS2 version 1.0
    public static final String VERSION = "1.0";

}
