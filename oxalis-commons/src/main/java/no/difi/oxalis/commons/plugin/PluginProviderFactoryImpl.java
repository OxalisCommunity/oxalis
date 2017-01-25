package no.difi.oxalis.commons.plugin;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import eu.peppol.lang.OxalisPluginException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author erlend
 */
class PluginProviderFactoryImpl implements PluginProviderFactory {

    private ClassLoader classLoader;

    @Inject
    public PluginProviderFactoryImpl(@Named("oxalis.ext.dir") Path endorsedDir) {
        if (!Files.isDirectory(endorsedDir) && Files.isReadable(endorsedDir)) {
            throw new OxalisPluginException(String.format("Unable to access directory '%s'.", endorsedDir));
        }

        classLoader = new URLClassLoader(
                findJarFiles(endorsedDir),
                Thread.currentThread().getContextClassLoader()
        );
    }

    @Override
    public <T> Provider<T> newProvider(Class<T> cls) {
        return new PluginProvider<>(classLoader, cls);
    }

    protected URL[] findJarFiles(Path endorsedDir) {
        List<URL> urls = new ArrayList<>();
        String glob = "*.{jar}";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(endorsedDir, glob)) {
            stream.forEach(path -> {
                try {
                    urls.add(path.toUri().toURL());
                } catch (MalformedURLException e) {
                    throw new OxalisPluginException(
                            String.format("Path '%s' can not be converted to URL: %s", path, e.getMessage()), e);
                }
            });

        } catch (IOException e) {
            throw new OxalisPluginException("Error during list of " + glob + " files in " + endorsedDir);
        }

        return urls.toArray(new URL[urls.size()]);
    }
}
