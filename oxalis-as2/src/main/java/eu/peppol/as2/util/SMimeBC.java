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

import eu.peppol.lang.OxalisSecurityException;
import no.difi.oxalis.commons.bouncycastle.BCHelper;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;

public class SMimeBC {

    private static JcaX509CertificateConverter x509CertificateConverter;

    static {
        BCHelper.registerProvider();

        x509CertificateConverter = new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME);
    }

    /**
     * http://stackoverflow.com/a/31557473/135001
     */
    public static X509Certificate verifySignature(Map hashes, byte[] signature) throws OxalisSecurityException {
        try {
            CMSSignedData signedData = new CMSSignedData(hashes, signature);

            Store store = signedData.getCertificates();
            SignerInformationStore signerInformationStore = signedData.getSignerInfos();

            for (SignerInformation signerInformation : signerInformationStore.getSigners()) {
                Collection<X509CertificateHolder> certCollection = store.getMatches(signerInformation.getSID());

                X509CertificateHolder certificateHolder = certCollection.iterator().next();
                X509Certificate certificate = x509CertificateConverter.getCertificate(certificateHolder);

                SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder()
                        .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                        .build(certificate);

                if (signerInformation.verify(verifier))
                    return certificate;
            }

            throw new OxalisSecurityException("Unable to verify signature.");
        } catch (CMSSignerDigestMismatchException e) {
            throw new OxalisSecurityException("Invalid message digest.", e);
        } catch (CMSException | CertificateException | OperatorCreationException e) {
            throw new OxalisSecurityException(e.getMessage(), e);
        }
    }
}
