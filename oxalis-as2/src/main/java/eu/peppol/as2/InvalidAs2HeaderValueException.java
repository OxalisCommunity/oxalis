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
