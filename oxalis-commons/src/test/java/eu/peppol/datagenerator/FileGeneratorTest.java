/*
 * Copyright (c) 2011,2012,2013,2015 UNIT4 Agresso AS.
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

package eu.peppol.datagenerator;

import eu.peppol.datagenerator.FileGenerator;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author steinar
 *         Date: 08.06.15
 *         Time: 21.07
 */
public class FileGeneratorTest {


    @Test
    public void generateBigFile() throws Exception {

        FileGenerator fileGenerator = new FileGenerator();
        long requestedSize = fileGenerator.MB * 1000;
        long l = fileGenerator.calculateNumberOfLines(requestedSize);

        System.out.println("Length of header:" + fileGenerator.header.length());
        System.out.println("Length of footer:     " + fileGenerator.footer.length());
        System.out.println("Length of catalogue line:" + fileGenerator.catalogueLine.length());
        System.out.println("Number of lines: " + l);

        File generate = fileGenerator.generate(File.createTempFile("PEPPOL-SAMPLE", ".xml"), requestedSize);

        System.out.println(generate.getCanonicalPath());

    }
}