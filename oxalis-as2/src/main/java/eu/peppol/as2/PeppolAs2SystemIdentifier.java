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

import eu.peppol.security.CommonName;

/**
 * @author steinar
 *         Date: 08.12.13
 *         Time: 20:48
 */
public class PeppolAs2SystemIdentifier extends  As2SystemIdentifier {

    public static final String AS2_SYSTEM_ID_PREFIX = "peppol-";

    public PeppolAs2SystemIdentifier(String as2Name) throws InvalidAs2SystemIdentifierException {
        super(as2Name);

        if (!as2Name.startsWith(AS2_SYSTEM_ID_PREFIX)) {
            throw new IllegalArgumentException("Invalid PEPPOL AS2 System Identifier, must have prefix " + AS2_SYSTEM_ID_PREFIX);
        }
    }

    public static final PeppolAs2SystemIdentifier valueOf(CommonName commonName) throws InvalidAs2SystemIdentifierException {
        return new PeppolAs2SystemIdentifier(AS2_SYSTEM_ID_PREFIX + commonName.toString());
    }
}
