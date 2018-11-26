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

package eu.sendregning.oxalis;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 08.01.2017
 *         Time: 13.53
 */
public class MainTest {


    private Path tempDirectory;

    private Path tempFile;

    private Path tempFile2;

    private Path fileHoldingFileNames;

    @BeforeMethod
    public void setUp() throws Exception {
        tempDirectory = Files.createTempDirectory("unit");

        tempFile = Files.createTempFile(tempDirectory, "TEST", ".xml");
        tempFile2 = Files.createTempFile(tempDirectory, "TEST2", ".xml");

        fileHoldingFileNames = Files.createTempFile("test", ".txt");

        List<String> fileNames = Arrays.asList(tempFile.toString(), tempFile2.toString());
        Files.write(fileHoldingFileNames, fileNames);
    }


    /**
     * Files may be located using any of the following specifications:
     *
     * <ul>
     *     <li>A single hyphen '-' indicates read list of files from stdin, one file per line</li>
     *     <li>A file referencing a file is, well a single file</li>
     *     <li>A file referencing a directory, indicates that all .xml files in that directory should be read </li>
     *     <li>A specification using '*' should be expanded</li>
     * </ul>
     * @throws Exception
     */
    @Test
    public void testLocateFiles() throws Exception {

        List<File> files = Main.locateFiles(tempDirectory.toString());

        assertEquals(files.size(), 2);

        files = Main.locateFiles(tempFile.toString());
        assertEquals(1, files.size());
    }

    @Test
    public void testStdin() throws Exception {

        InputStream saved = System.in;

        System.setIn( new FileInputStream(fileHoldingFileNames.toFile()) );

        List<File> files = Main.locateFiles("-");
        assertEquals(2, files.size());
        System.setIn(saved);

    }
}

