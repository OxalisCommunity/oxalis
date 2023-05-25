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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author erlend
 */
public class ClassLoaderUtilsTest {

    private ClassLoader current = Thread.currentThread().getContextClassLoader();

    @Test
    public void simpleConstructor() {
        new ClassLoaderUtils();
    }

    @Test
    public void simpleNull() {
        Assert.assertEquals(ClassLoaderUtils.initiate(null), current);
    }

    @Test
    public void simpleJavaHome() {
        Path path = Paths.get(System.getProperty("java.home"), "lib");
        Assert.assertNotEquals(ClassLoaderUtils.initiate(path), current);
    }

    @Test
    public void simpleRT() {
        FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
        Path path = fs.getPath("modules", "java.base", "java/lang/Object.class");
        Assert.assertNotEquals(ClassLoaderUtils.initiate(path), current);
    }

    @Test(expectedExceptions = OxalisPluginException.class)
    public void triggerException() {
        Path path = Paths.get(System.getProperty("java.home"), "completely", "invalid", "folder");
        ClassLoaderUtils.initiate(path);
    }
}
