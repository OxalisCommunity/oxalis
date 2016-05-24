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

import eu.peppol.security.CommonName;

/**
 * @author steinar
 *         Date: 08.12.13
 *         Time: 20:48
 */
public class PeppolAs2SystemIdentifier extends  As2SystemIdentifier {

    public static final String AS2_SYSTEM_ID_PREFIX = "";

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
