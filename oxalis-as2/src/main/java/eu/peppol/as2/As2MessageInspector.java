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

import com.google.inject.Inject;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;

import javax.security.auth.x500.X500Principal;

/**
 * @author steinar
 *         Date: 08.10.13
 *         Time: 11:09
 */
public class As2MessageInspector {


    private final KeystoreManager keystoreManager;

    @Inject
    public As2MessageInspector(KeystoreManager keystoreManager) {
        this.keystoreManager = keystoreManager;
    }


    public void validate(As2Message as2Message) throws InvalidAs2MessageException {


        compareAs2FromHeaderWithCertificateCommonName(as2Message);

        // TODO : compare the value of the AS2-To: header with the CN attribute of our own certificate for equality
    }

    /** Compares the value of the "AS2-From" header with the value of the CN= attribute of the inbound certificate. */
    private static void compareAs2FromHeaderWithCertificateCommonName(As2Message as2Message) throws InvalidAs2MessageException {

        // Retrieves the CN=AP_......, O=X......, C=.... from the certificate
        X500Principal x500Principal = as2Message.getSignedMimeMessage().getSignersX509Certificate().getSubjectX500Principal();
        CommonName sendersCommonName = CommonName.valueOf(x500Principal);

        // Verifies that the value of AS2-From header equals the value of the CN attribute from the signers certificate
        PeppolAs2SystemIdentifier as2SystemIdentifierFromCertificate = PeppolAs2SystemIdentifier.valueOf(sendersCommonName);
        if (!as2SystemIdentifierFromCertificate.equals(as2Message.getAs2From())) {
            throw new InvalidAs2MessageException("The signers CN '" + as2SystemIdentifierFromCertificate.toString() + "'does not compare to the AS2-From header '" + as2Message.getAs2From().toString()+"'");
        }

    }

}
