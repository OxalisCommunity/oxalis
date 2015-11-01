/*
 * Copyright (c) 2015 Steinar Overbeck Cook
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

import eu.peppol.as2.As2ReceiptData;
import eu.peppol.persistence.GenericTransportReceipt;

import javax.mail.internet.MimeMessage;

/**
 * @author steinar
 *         Date: 01.11.2015
 *         Time: 21.26
 */
public class As2GenericTransportReceiptImpl implements GenericTransportReceipt {

    public As2GenericTransportReceiptImpl(As2ReceiptData as2ReceiptData, MimeMessage mimeMessage) {
    }
}
