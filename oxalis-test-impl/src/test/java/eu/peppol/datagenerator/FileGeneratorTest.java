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

package eu.peppol.datagenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author steinar
 *         Date: 08.06.15
 *         Time: 21.07
 */
public class FileGeneratorTest {

    public static final Logger log = LoggerFactory.getLogger(FileGeneratorTest.class);

    @Test
    public void simpleConstructor() {
        new FileGenerator();
    }

    @Test
    public void generateBigFile() throws Exception {
        long requestedSize = FileGenerator.MB * 1000;
        long l = FileGenerator.calculateNumberOfLines(requestedSize);

        log.debug("Length of header:" + FileGenerator.header.length());
        log.debug("Length of footer:     " + FileGenerator.footer.length());
        log.debug("Length of catalogue line:" + FileGenerator.catalogueLine.length());
        log.debug("Number of lines: " + l);

        File outputFile = FileGenerator.generate(requestedSize);

        log.debug(outputFile.getCanonicalPath());

        outputFile.delete();
    }
}
