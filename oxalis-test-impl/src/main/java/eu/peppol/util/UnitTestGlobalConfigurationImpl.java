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

package eu.peppol.util;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

/**
 * Provides a fake GlobalConfiguration instance, which works with our unit tests requiring access to an environment
 * in which a certificate is available.
 * <p>
 * Created by soc on 11.12.2015.
 */
@Singleton
public class UnitTestGlobalConfigurationImpl implements GlobalConfiguration {

    public static final Logger log = LoggerFactory.getLogger(UnitTestGlobalConfigurationImpl.class);

    public static final String FAKE_OXALIS_GLOBAL_PROPERTIES = "fake-oxalis-global.properties";
    private static Path ourCertificateKeystore;
    private final Path oxalisHomeDir;
    private final Path inboundDirectory;

    // In testing the default is to allow overrides
    private Boolean transmissionBuilderOverride = true;


    private UnitTestGlobalConfigurationImpl() {
        oxalisHomeDir = createTemporaryDirectory();

        inboundDirectory = createTempInboundDirectory();

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
        Path tempDirectory = null;
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

    static InputStream testConfigProperties() {

        InputStream resourceAsStream = UnitTestGlobalConfigurationImpl.class.getClassLoader().getResourceAsStream(FAKE_OXALIS_GLOBAL_PROPERTIES);
        if (resourceAsStream == null) {
            throw new IllegalStateException("Unable to locate " + FAKE_OXALIS_GLOBAL_PROPERTIES + " in classpath ");
        }

        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(resourceAsStream, "UTF-8"));

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return resourceAsStream;

    }

    private Path createTempInboundDirectory() {
        try {
            Path inbound = Files.createTempDirectory(oxalisHomeDir, "inbound");
            return inbound;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create temporary directory for inbound messages", e);
        }
    }

    @Override
    public String getJdbcDriverClassName() {
        return null;
    }

    @Override
    public String getJdbcConnectionURI() {
        return null;
    }

    @Override
    public String getJdbcUsername() {
        return null;
    }

    @Override
    public String getJdbcPassword() {
        return null;
    }

    @Override
    public String getDataSourceJndiName() {
        return null;
    }

    @Override
    public String getJdbcDriverClassPath() {
        return null;
    }

    @Override
    public String getJdbcDialect() {
        return null;
    }

    @Override
    public String getKeyStoreFileName() {
        return ourCertificateKeystore.toString();
    }

    @Override
    public String getKeyStorePassword() {
        return "peppol";
    }

    @Override
    public String getTrustStorePassword() {
        return "peppol";
    }

    @Override
    public String getInboundMessageStore() {
        return inboundDirectory.toString();
    }

    @Override
    public String getPersistenceClassPath() {
        return null;
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
    public Integer getConnectTimeout() {
        return null;
    }

    @Override
    public Integer getReadTimeout() {
        return null;
    }

    @Override
    public File getOxalisHomeDir() {
        return oxalisHomeDir.toFile();
    }

    @Override
    public String getSmlHostname() {
        return null;
    }

    @Override
    public String getHttpProxyHost() {
        return null;
    }

    @Override
    public String getHttpProxyPort() {
        return null;
    }

    @Override
    public String getProxyUser() {
        return null;
    }

    @Override
    public String getProxyPassword() {
        return null;
    }

    @Override
    public String getValidationQuery() {
        return null;
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
