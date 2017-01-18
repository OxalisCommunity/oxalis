package no.difi.oxalis.commons.filesystem;

import com.google.inject.*;
import com.google.inject.name.Names;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class FileSystemModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(FileSystem.class, Names.named("default"))).toInstance(FileSystems.getDefault());
    }

    @Provides
    @Singleton
    protected FileSystem getFileSystem(Injector injector) {
        return injector.getInstance(Key.get(FileSystem.class, Names.named("default")));
    }
}
