/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package eu.peppol.util;

import com.google.inject.Singleton;
import no.difi.oxalis.api.config.GlobalConfiguration;
import no.difi.oxalis.api.config.OperationalMode;
import no.difi.oxalis.api.config.PropertyDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Provides a fake GlobalConfiguration instance, which works with our unit tests requiring access to an environment
 * in which a certificate is available.
 * <p>
 * Created by soc on 11.12.2015.
 */
@Singleton
public class UnitTestGlobalConfigurationImpl implements GlobalConfiguration {

    public static final Logger log = LoggerFactory.getLogger(UnitTestGlobalConfigurationImpl.class);

    private static Path ourCertificateKeystore;

    private final Path oxalisHomeDir;

    // In testing the default is to allow overrides
    private Boolean transmissionBuilderOverride = true;


    private UnitTestGlobalConfigurationImpl() {
        oxalisHomeDir = createTemporaryDirectory();

        // Copies the dummy keystore into our temporary directory
        ourCertificateKeystore = copyResourceUsingBaseName("security/oxalis-dummy-keystore.jks", oxalisHomeDir, "oxalis-keystore.jks");
    }

    public static GlobalConfiguration createInstance() {
        return new UnitTestGlobalConfigurationImpl();
    }

    private static Path copyResourceUsingBaseName(String resourceName, Path tempDirectory, String destinationFilename) {

        Path destination = tempDirectory.resolve(destinationFilename);
        try (InputStream resourceAsStream = UnitTestGlobalConfigurationImpl.class.getClassLoader().getResourceAsStream(resourceName);) {

            Files.copy(resourceAsStream, destination);

            return destination;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to copy " + resourceName + " to " + destination + ", cause:" + e.getMessage(), e);
        }
    }

    static Path createTemporaryDirectory() {
        Path tempDirectory;
        try {

            tempDirectory = Files.createTempDirectory("unit-test");

            final Path finalTempDirectory = tempDirectory;
            Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!dir.endsWith(finalTempDirectory)) {
                        Files.deleteIfExists(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            System.out.println("Created temporary directory " + tempDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create temp directory with prefix 'unit-test'", e);
        }
        return tempDirectory;
    }

    @Override
    public String getKeyStoreFileName() {
        return ourCertificateKeystore.toString();
    }

    @Override
    public String getInboundLoggingConfiguration() {
        return null;
    }

    @Override
    public OperationalMode getModeOfOperation() {
        return OperationalMode.TEST;
    }

    @Override
    public File getOxalisHomeDir() {
        return oxalisHomeDir.toFile();
    }

    @Override
    public Boolean isTransmissionBuilderOverride() {
        return transmissionBuilderOverride;
    }

    @Override
    public void setTransmissionBuilderOverride(Boolean transmissionBuilderOverride) {
        if (this.transmissionBuilderOverride != transmissionBuilderOverride) {
            log.warn("Property " + PropertyDef.TRANSMISSION_BUILDER_OVERRIDE.getPropertyName() + " is being changed to " + transmissionBuilderOverride + " from " + this.transmissionBuilderOverride);
        }

        this.transmissionBuilderOverride = transmissionBuilderOverride;
    }
}
