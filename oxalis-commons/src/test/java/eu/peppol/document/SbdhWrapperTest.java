/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
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

import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 08.11.13
 *         Time: 16:30
 */
public class SbdhWrapperTest {

    @Test
    public void testWrap() throws Exception {

        InputStream resourceAsStream = SbdhWrapperTest.class.getClassLoader().getResourceAsStream("ehf-invoice-no-sbdh.xml");
        assertNotNull(resourceAsStream);

        SbdhWrapper sbdhWrapper = new SbdhWrapper();
        byte[] wrap = sbdhWrapper.wrap(resourceAsStream);

        String s = new String(wrap, "UTF-8");
        System.out.println(s);
    }
}
