/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.commons.filesystem;

import network.oxalis.api.lang.OxalisPluginException;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

/**
 * Util class used to support use of class loader in Oxalis.
 *
 * @author erlend
 * @since 4.0.0
 */
public class ClassLoaderUtils {

    /**
     * Inititiates a {@link ClassLoader} from {@link Path}. Behaviour changes based upon input:
     * <p>
     * <ul>
     * <li>Path is `null` - Returns current class loader.</li>
     * <li>Path is a file - Returns class loader using that file only.</li>
     * <li>Path is a directory - Returns class loader using all jar-files in directory.</li>
     * <li>Otherwise is exception thrown.</li>
     * </ul>
     *
     * @param path Path to be used when initiating class loader.
     * @return Class loader ready for use.
     */
    public static ClassLoader initiate(Path path) {
        // `null` -> Return current class loader.
        if (path == null)
            return Thread.currentThread().getContextClassLoader();

        // Is directory -> Return class loader with all jars in directory.
        else if (Files.isDirectory(path))
            return new URLClassLoader(
                    findJarFiles(path),
                    Thread.currentThread().getContextClassLoader()
            );

        // Is file -> Return class loader using file.
        else if (Files.isRegularFile(path))
            return new URLClassLoader(
                    new URL[]{FileUtils.toUrl(path)},
                    Thread.currentThread().getContextClassLoader()
            );

        // Otherwise throw exception.
        else
            throw new OxalisPluginException(String.format("Unable to load class loader for '%s'.", path));
    }

    protected static URL[] findJarFiles(Path directory) {
        String glob = "*.{jar}";

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, glob)) {
            return StreamSupport.stream(stream.spliterator(), false)
                    .map(FileUtils::toUrl)
                    .toArray(URL[]::new);
        } catch (IOException e) {
            throw new OxalisPluginException(
                    String.format("Error during list of '%s' files in '%s'.", glob, directory));
        }
    }
}
