package eu.peppol.security;

import com.google.inject.Inject;
import eu.peppol.util.OperationalMode;
import eu.peppol.util.RuntimeConfigurationModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 08.08.13
 *         Time: 20:19
 */
@Guice(modules = {RuntimeConfigurationModule.class})
public class PeppolTrustStoreTest {

    @Inject
    PeppolTrustStore peppolTrustStore;

    @Test
    public void version1InProductionMode() throws Exception {

        // In Transitional version and Production mode, two keystores should be loaded
        List<PeppolTrustStore.TrustStoreResource> trustStoreResources = peppolTrustStore.resourceNamesFor(PkiVersion.V1, OperationalMode.PRODUCTION);
        assertEquals(trustStoreResources.size(), 1);

        assertTrue(trustStoreResources.contains(PeppolTrustStore.TrustStoreResource.V2_TEST));
    }


    @Test
    public void transitionalVersionInProductionMode() throws Exception {

        // In Transitional version and Production mode, two keystores should be loaded
        List<PeppolTrustStore.TrustStoreResource> trustStoreResources = peppolTrustStore.resourceNamesFor(PkiVersion.T, OperationalMode.PRODUCTION);
        assertEquals(trustStoreResources.size(), 2);

        assertTrue(trustStoreResources.contains(PeppolTrustStore.TrustStoreResource.V2_PRODUCTION));
        assertTrue(trustStoreResources.contains(PeppolTrustStore.TrustStoreResource.V2_TEST));

    }

    @Test
    public void version2InProductionMode() throws Exception {

        // In Transitional version and Production mode, two keystores should be loaded
        List<PeppolTrustStore.TrustStoreResource> trustStoreResources = peppolTrustStore.resourceNamesFor(PkiVersion.V2, OperationalMode.PRODUCTION);
        assertEquals(trustStoreResources.size(), 1);

        assertTrue(trustStoreResources.contains(PeppolTrustStore.TrustStoreResource.V2_PRODUCTION));
    }

}
