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
 * @author steinar
 *         Date: 09.10.13
 *         Time: 13:37
 */
public class InvalidAs2HeaderValueException extends InvalidAs2MessageException {

    private final As2Header headerName;
    private final String value;

    public InvalidAs2HeaderValueException(As2Header headerName, String value) {
        super("Invalid value for As2Header " + headerName + ": '" + value + "'");
        this.headerName = headerName;
        this.value = value;
    }

    public As2Header getHeaderName() {
        return headerName;
    }

    public String getValue() {
        return value;
    }
}
