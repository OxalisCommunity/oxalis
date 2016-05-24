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
package eu.peppol.outbound.ssl;

import eu.peppol.outbound.util.Log;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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

    private X509TrustManager defaultTrustManager = null;

    public AccessPointX509TrustManager(Set<String> acceptedCommonNames, X509Certificate acceptedRootCertificate) {
        this.rootCertificate = acceptedRootCertificate;
        this.commonNames = acceptedCommonNames;

        // Locates and saves the default trust manager, i.e. the one supplied with the Java runtime
        defaultTrustManager = locateAndSaveDefaultTrustManager();

    }

    private X509TrustManager locateAndSaveDefaultTrustManager() {
        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        try {
            TrustManagerFactory instance = TrustManagerFactory.getInstance(algorithm);
            instance.init((KeyStore) null); // Initialises the trust manager factory with the default CA certs installed
            int length = instance.getTrustManagers().length;
            TrustManager[] trustManagers = instance.getTrustManagers();
            for (TrustManager trustManager : trustManagers) {
                if (trustManager instanceof X509TrustManager) {
                    return  (X509TrustManager) trustManager;
                }
            }

        } catch (NoSuchAlgorithmException e) {
            Log.error("Unable to obtain instances of the TrustManagerFactory for algorithm " + algorithm);
        } catch (KeyStoreException e) {
            Log.error("Unable to initialize the trust manager");
        }
        return null;
    }

    public final void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        Log.debug("Checking client certificates");
        checkPrincipal(chain);
    }

    public final void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        for (X509Certificate x509Certificate : chain) {
            Log.debug("Inspecting peer certificate " + x509Certificate.getSubjectX500Principal() + ", issued by " + x509Certificate.getIssuerX500Principal());
        }

        // Invokes the default JSSE Trust Manager in order to check the SSL peer certificate.
        try {
            if (defaultTrustManager != null){
                defaultTrustManager.checkServerTrusted(chain, authType);
            } else {
                Log.warn("No default trust manager established upon creation of " + this.getClass().getSimpleName());
            }
        } catch (CertificateException e) {
            X509Certificate x509Certificate = chain[0];
            Log.warn("Server SSL sertificate " + x509Certificate + " is not trusted: " + e + "\nThis cause might be a missing root certificate in your local truststore");
        }
        checkPrincipal(chain);
        Log.debug("Void SSL server certificate check performed.");
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
