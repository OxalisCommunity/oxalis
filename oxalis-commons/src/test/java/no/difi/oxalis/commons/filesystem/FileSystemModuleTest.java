package no.difi.oxalis.commons.filesystem;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.FileSystem;

public class FileSystemModuleTest {

    @Test
    public void simple() {
        Injector injector = Guice.createInjector(new FileSystemModule());
        Assert.assertNotNull(injector.getInstance(FileSystem.class));
    }
}
