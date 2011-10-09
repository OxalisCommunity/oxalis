/*
 * Version: MPL 1.1/EUPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Copyright The PEPPOL project (http://www.peppol.eu)
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL
 * (the "Licence"); You may not use this work except in compliance
 * with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * If you wish to allow use of your version of this file only
 * under the terms of the EUPL License and not to allow others to use
 * your version of this file under the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and
 * other provisions required by the EUPL License. If you do not delete
 * the provisions above, a recipient may use your version of this file
 * under either the MPL or the EUPL License.
 */
package eu.peppol.outbound.client;

import eu.peppol.outbound.util.Log;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

/**
 * The AccessPointX509TrustManager is pointed to authenticate the remote side
 * when using SSL.
 *
 * @author Alexander Aguirre Julcapoma(alex@alfa1lab.com)
 *         Jose Gorvenia Narvaez(jose@alfa1lab.com)
 */
public class AccessPointX509TrustManager implements X509TrustManager {

    /**
     * The permitted remote common names, or null if no restriction.
     */
    private Set<String> commonNames;

    /**
     * The accepted issuer.
     */
    private X509Certificate rootCertificate;

    /**
     * Constructor with parameters.
     *
     * @param acceptedCommonNames     A Collection(Set) of Names accepted.
     * @param acceptedRootCertificate Represents a Certificate.
     * @throws Exception Throws an Exception.
     */
    public AccessPointX509TrustManager(final Set<String> acceptedCommonNames,
                                       final X509Certificate acceptedRootCertificate) throws Exception {

        this.rootCertificate = acceptedRootCertificate;
        this.commonNames = acceptedCommonNames;
    }

    /**
     * Check if client is trusted.
     *
     * @param chain    an array of X509Certificate holding the certificates.
     * @param authType authentication type.
     * @throws CertificateException Throws a CertificateException.
     */
    public final void checkClientTrusted(final X509Certificate[] chain, final String authType)
            throws CertificateException {
        Log.info("Checking client certificates");
        check(chain);
    }

    /**
     * Check if server is trusted.
     *
     * @param chain    Array of Certificates.
     * @param authType is never used
     * @throws CertificateException Error with certificates.
     */
    public final void checkServerTrusted(final X509Certificate[] chain,
                                         final String authType)
            throws CertificateException {
        Log.info("Checking server certificates");
        check(chain);
    }

    /**
     * Returns an array of X509Certificate objects which are trusted for
     * authenticating peers.
     *
     * @return X509Certificate array containing the accepted root certificates.
     */
    public final X509Certificate[] getAcceptedIssuers() {

        X509Certificate[] certs = new X509Certificate[1];
        certs[0] = rootCertificate;

        return certs;
    }

    /**
     * Checks chain.
     *
     * @param chain Array of certificates.
     * @throws CertificateException Exception for Certificates.
     */
    private void check(final X509Certificate[] chain)
            throws CertificateException {

        checkPrincipal(chain);
    }

    /**
     * Check Principal.
     *
     * @param chain Array of Certificates.
     * @throws CertificateException Exception for Certificates.
     */
    private void checkPrincipal(final X509Certificate[] chain)
            throws CertificateException {

        boolean commonNameOK = false;

        if (commonNames == null) {
            commonNameOK = true;
        } else {

            String[] array = chain[0].getSubjectX500Principal().toString().split(",");

            for (String tok : array) {

                int x = tok.indexOf("CN=");
                if (x >= 0) {
                    String curCN = tok.substring(x + 3);
                    if (commonNames.contains(curCN)) {
                        commonNameOK = true;
                        StringBuilder logappender = new StringBuilder();
                        logappender.append("Accepted issuer: ");
                        logappender.append(tok.substring(x + 3));

                        Log.info(logappender.toString());
                        Log.info("Accepted issuer: " + tok.substring(x + 3));

                        break;
                    }
                }
            }
        }

        if (!commonNameOK) {
            StringBuilder logappender = new StringBuilder();
            logappender.append("No accepted issuer: ");
            logappender.append(chain[0].getSubjectX500Principal().toString());

            Log.error(logappender.toString());

            throw new CertificateException("Remote principal is not trusted");
        }
    }
}
