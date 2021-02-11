package network.oxalis.test.filesystem;

import network.oxalis.api.filesystem.HomeDetector;
import org.kohsuke.MetaInfServices;

import java.io.File;
import java.nio.file.Paths;

/**
 * @author erlend
 */
@MetaInfServices
public class FakeHomeDetector implements HomeDetector {

    @Override
    public File detect() {
        try {
            return Paths.get(getClass().getResource("/oxalis_home/fake-oxalis.conf").toURI()).getParent().toFile();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
