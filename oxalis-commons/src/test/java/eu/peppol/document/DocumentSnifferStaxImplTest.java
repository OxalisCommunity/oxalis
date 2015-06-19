/*
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
import org.testng.annotations.Test;

import java.io.*;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 18.06.15
 *         Time: 16.36
 */
public class DocumentSnifferStaxImplTest {


    @Test
    public void parseSampleFile() throws Exception {

        FileGenerator fileGenerator = new FileGenerator();
        File xmlSampleFile = fileGenerator.generate(FileGenerator.MB * 10L);


        FileInputStream fileInputStream = new FileInputStream(xmlSampleFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        DocumentSnifferStaxImpl documentSnifferStax = new DocumentSnifferStaxImpl(bufferedInputStream);


    }
}