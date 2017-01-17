package no.difi.oxalis.commons.evidence;

import com.google.inject.Inject;
import no.difi.oxalis.api.evidence.EvidenceFactory;
import no.difi.oxalis.commons.guice.TestOxalisKeystoreModule;
import no.difi.oxalis.commons.mode.ModeModule;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

/**
 * @author erlend
 */
@Guice(modules = {EvidenceModule.class, ModeModule.class, TestOxalisKeystoreModule.class})
public class RemEvidenceFactoryTest {

    @Inject
    private EvidenceFactory evidenceFactory;

    @Test
    public void simple() {
        Assert.assertNotNull(evidenceFactory);
    }
}
