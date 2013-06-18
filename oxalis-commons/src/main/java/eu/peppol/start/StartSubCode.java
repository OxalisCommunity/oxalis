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

package eu.peppol.start;

/**
 * @author steinar
 *         Date: 08.06.13
 *         Time: 11:41
 */
public enum StartSubCode {

    CHANNEL_FULL("bden:ChannelFull"),
    UNKNOWN_ENDPOINT("bden:UnknownEndpoint"),
    SECURITY_ERROR("bden:SecurityFault"),
    DOC_TYPE_NOT_ACCEPTED("bden:DocumentTypeNotAccepted"),
    SERVER_ERROR("bden:ServerError")
    ;

    private final String textValue;

    StartSubCode(String text) {
        this.textValue = text;
    }

    @Override
    public String toString() {
        return textValue;
    }

    public static StartSubCode valueFromCode(String textRepresentation) {
        for (StartSubCode startSubCode : StartSubCode.values()) {
            if (startSubCode.textValue.equals(textRepresentation)) {
                return startSubCode;
            }
        }

        throw new IllegalArgumentException("Invalid START subcode: " + textRepresentation);
    }
}
