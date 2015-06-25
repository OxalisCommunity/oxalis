/*
 * Copyright (c) 2015 Steinar Overbeck Cook
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.document;

import eu.peppol.datagenerator.FileGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 24.06.15
 *         Time: 15.58
 */
public class SbdhFastParserTest {

    public static final Logger log = LoggerFactory.getLogger(SbdhFastParserTest.class);
    public static final String EHF_INVOICE_NO_SBDH_XML = "ehf-invoice-no-sbdh.xml";
    private File xmlSampleFile;

    @BeforeMethod
    public void setUp() {
        xmlSampleFile = FileGenerator.generate(FileGenerator.MB * 100L);
    }

    @AfterMethod
    public void tearDown() {
        if (xmlSampleFile.exists() && xmlSampleFile.isFile()) {
            xmlSampleFile.delete();
        }

    }

    @Test
    public void parseMediumSizedFile() throws Exception {
        FileInputStream fileInputStream = new FileInputStream(xmlSampleFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        long start = System.currentTimeMillis();
        SbdhFastParser sbdhFastParser = new SbdhFastParser();
        StandardBusinessDocumentHeader sbdh = sbdhFastParser.parse(fileInputStream);
        long stop = System.currentTimeMillis();
        long elapsed = stop - start;

        log.debug("Time elapsed : " + elapsed + "ms");
        boolean sbdhDetected = sbdh != null;

        assertTrue(bufferedInputStream.available() > 0);
        assertTrue(sbdhDetected, "SBDH was not detected");
    }


    @Test
    public void parseXmlFileWithoutSBDH() {
        InputStream resourceAsStream = SbdhFastParser.class.getClassLoader().getResourceAsStream(EHF_INVOICE_NO_SBDH_XML);
        if (resourceAsStream == null) {
            throw new IllegalStateException("Unable to find " + EHF_INVOICE_NO_SBDH_XML + " in classpath");
        }

        SbdhFastParser sbdhFastParser = new SbdhFastParser();
        StandardBusinessDocumentHeader sbdh = sbdhFastParser.parse(resourceAsStream);
        assertNull(sbdh);

    }
}