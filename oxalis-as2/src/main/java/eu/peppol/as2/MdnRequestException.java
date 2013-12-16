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
 * Indicates that the MDN request could not be handled. I.e. the requested protocol is not available or there
 * was an error during the parsing of the header "disposition-notification-options"
 *
 * @author steinar
 *         Date: 17.10.13
 *         Time: 22:27
 */
public class MdnRequestException extends Exception {

    public MdnRequestException(String msg) {
        super(msg);
    }
}
