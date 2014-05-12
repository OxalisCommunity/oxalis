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

import javax.security.auth.x500.X500Principal;

/**
 * @author steinar
 *         Date: 08.10.13
 *         Time: 11:09
 */
public class As2MessageInspector {

    public static SignedMimeMessageInspector validate(As2Message as2Message) throws InvalidAs2MessageException {

        SignedMimeMessageInspector SignedMimeMessageInspector = new SignedMimeMessageInspector(as2Message.getMimeMessage());

        compareAs2FromHeaderWithCertificateCommonName(as2Message, SignedMimeMessageInspector);

        // TODO : compare the value of the AS2-To: header with the CN attribute of our own certificate for equality

        return SignedMimeMessageInspector;
    }

    /** Compares the value of the "AS2-From" header with the value of the CN= attribute of the inbound certificate. */
    private static void compareAs2FromHeaderWithCertificateCommonName(As2Message as2Message, SignedMimeMessageInspector SignedMimeMessageInspector) throws InvalidAs2MessageException {

        // Retrieves the CN=AP_......, O=X......, C=.... from the certificate
        X500Principal x500Principal = SignedMimeMessageInspector.getSignersX509Certificate().getSubjectX500Principal();
        CommonName sendersCommonName = CommonName.valueOf(x500Principal);

        // Verifies that the value of AS2-From header equals the value of the CN attribute from the signers certificate
        PeppolAs2SystemIdentifier as2SystemIdentifierFromCertificate = PeppolAs2SystemIdentifier.valueOf(sendersCommonName);
        if (!as2SystemIdentifierFromCertificate.equals(as2Message.getAs2From())) {
            throw new InvalidAs2MessageException("The signers CN '" + as2SystemIdentifierFromCertificate.toString() + "'does not compare to the AS2-From header '" + as2Message.getAs2From().toString()+"'");
        }

    }

}
