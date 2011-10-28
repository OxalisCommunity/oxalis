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
package eu.peppol.outbound.ssl;

import eu.peppol.outbound.util.Log;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

/**
 * The AccessPointX509TrustManager is pointed to authenticate the remote side when using SSL.
 *
 * @author Alexander Aguirre Julcapoma(alex@alfa1lab.com)
 *         Jose Gorvenia Narvaez(jose@alfa1lab.com)
 */
public class AccessPointX509TrustManager implements X509TrustManager {

    private Set<String> commonNames;
    private X509Certificate rootCertificate;

    public AccessPointX509TrustManager(Set<String> acceptedCommonNames, X509Certificate acceptedRootCertificate) {
        this.rootCertificate = acceptedRootCertificate;
        this.commonNames = acceptedCommonNames;
    }

    public final void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        Log.debug("Checking client certificates");
        checkPrincipal(chain);
    }

    public final void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        checkPrincipal(chain);
        Log.debug("Void SSL server certificate check OK");
    }

    public final X509Certificate[] getAcceptedIssuers() {
        Log.debug("Returning trusted root certificates");
        return new X509Certificate[]{rootCertificate};
    }

    private void checkPrincipal(final X509Certificate[] chain) throws CertificateException {

        if (commonNames == null) {
            return;
        }

        String[] array = chain[0].getSubjectX500Principal().toString().split(",");

        for (String s : array) {

            int x = s.indexOf("CN=");

            if (x >= 0) {
                String curCN = s.substring(x + 3);

                if (commonNames.contains(curCN)) {
                    StringBuilder logappender = new StringBuilder();
                    logappender.append("Accepted issuer: ");
                    logappender.append(s.substring(x + 3));

                    Log.info(logappender.toString());
                    Log.info("Accepted issuer: " + s.substring(x + 3));

                    return;
                }
            }
        }

        Log.error("No accepted issuer: " + chain[0].getSubjectX500Principal());
        throw new CertificateException("Remote principal is not trusted");
    }
}
