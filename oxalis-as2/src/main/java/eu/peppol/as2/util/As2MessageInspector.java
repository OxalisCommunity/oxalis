/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package eu.peppol.as2.util;

import eu.peppol.as2.lang.InvalidAs2MessageException;
import eu.peppol.as2.model.As2Message;
import no.difi.oxalis.commons.security.CertificateUtils;

/**
 * @author steinar
 *         Date: 08.10.13
 *         Time: 11:09
 */
public class As2MessageInspector {

    public static void validate(As2Message as2Message) throws InvalidAs2MessageException {
        compareAs2FromHeaderWithCertificateCommonName(as2Message);

        // TODO : compare the value of the AS2-To: header with the CN attribute of our own certificate for equality
    }

    /**
     * Compares the value of the "AS2-From" header with the value of the CN= attribute of the inbound certificate.
     */
    private static void compareAs2FromHeaderWithCertificateCommonName(As2Message as2Message) throws InvalidAs2MessageException {
        // Retrieves the CN=AP_......, O=X......, C=.... from the certificate
        String sendersCommonName = CertificateUtils.extractCommonName(as2Message.getSignedMimeMessage().getSignersX509Certificate());

        // Verifies that the value of AS2-From header equals the value of the CN attribute from the signers certificate
        if (!sendersCommonName.equals(as2Message.getAs2From())) {
            throw new InvalidAs2MessageException("The signers CN '" + sendersCommonName + "'does not compare to the AS2-From header '" + as2Message.getAs2From() + "'");
        }
    }
}
