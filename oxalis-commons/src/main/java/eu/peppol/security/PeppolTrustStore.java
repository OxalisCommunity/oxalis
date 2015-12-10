package eu.peppol.security;

import com.google.inject.Inject;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OperationalMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles creation of PEPPOL trust store based upon the PKI version and the mode of operation.
 *
 * @author steinar
 *         Date: 08.08.13
 *         Time: 19:49
 */
public class PeppolTrustStore extends KeyStoreUtil {

    public static final Logger log = LoggerFactory.getLogger(PeppolTrustStore.class);
    private final GlobalConfiguration globalConfiguration;

    @Inject
    public PeppolTrustStore(GlobalConfiguration globalConfiguration) {

        this.globalConfiguration = globalConfiguration;
    }

    /**
     * Combines and loads the built-in PEPPOL trust stores, assuming they all have the same password.
     *
     * @param pkiVersion
     * @param operationalMode
     * @return
     */
    public KeyStore loadTrustStoreFor(PkiVersion pkiVersion, OperationalMode operationalMode) {

        // Figures out which trust store resources to load depending upon the mode of operation and
        // which PKI version we are using.
        List<TrustStoreResource> trustStoreResources = resourceNamesFor(pkiVersion, operationalMode);

        List<String> resourceNames = fetchResourceNames(trustStoreResources);

        if (log.isDebugEnabled()) {

            StringBuilder sb = new StringBuilder("Loading and combining trust stores:");
            for (String resourceName : resourceNames) {
                sb.append(" ").append(resourceName);
            }
            log.debug(sb.toString());
        }

        String trustStorePassword = globalConfiguration.getTrustStorePassword();

        List<KeyStore> trustStores = KeyStoreUtil.loadKeyStores(resourceNames, trustStorePassword);

        KeyStore keyStore = KeyStoreUtil.combineTrustStores(trustStores.toArray(new KeyStore[]{}));

        return keyStore;
    }


    List<String> fetchResourceNames(List<TrustStoreResource> trustStoreResources) {
        List<String> resourceNames = new ArrayList<String>();
        for (TrustStoreResource trustStoreResource : trustStoreResources) {
            resourceNames.add(trustStoreResource.getResourcename());
        }
        return resourceNames;
    }


    List<TrustStoreResource> resourceNamesFor(PkiVersion pkiVersion, OperationalMode operationalMode) {
        List<TrustStoreResource> trustStoresToLoad = new ArrayList<TrustStoreResource>();
        switch (operationalMode) {
            case TEST:
                trustStoresToLoad.add(TrustStoreResource.V2_TEST);
                break;
            
            case PRODUCTION:
                switch (pkiVersion) {
                    case V1:
                        trustStoresToLoad.add(TrustStoreResource.V2_TEST);
                        break;
                    case T:
                        trustStoresToLoad.add(TrustStoreResource.V2_TEST);
                        trustStoresToLoad.add(TrustStoreResource.V2_PRODUCTION);
                        break;
                    case V2:
                        trustStoresToLoad.add(TrustStoreResource.V2_PRODUCTION);
                        break;
                }
                break;
            default:
                throw new IllegalStateException("No configuration for operational mode " + operationalMode.name());
        }
        return trustStoresToLoad;
    }

    public  enum TrustStoreResource {
        V2_TEST("truststore-test.jks"),
        V2_PRODUCTION("truststore-production.jks"),
        ;

        private final String resourceName;

        TrustStoreResource(String s) {
            this.resourceName = s;
        }

        public String getResourcename() {
            return resourceName;
        }
    }
}
