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

package eu.peppol.smp;

import eu.peppol.util.ConnectionException;
import eu.peppol.util.TryAgainLaterException;
import eu.peppol.util.Util;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author steinar
 *         Date: 18.12.13
 *         Time: 22:45
 */
public class SmpContentRetrieverImplTest {

    @Test(expectedExceptions = ConnectionException.class)
    public void test404() throws Exception {
        new SmpContentRetrieverImpl().getUrlContent(new URL("http://smp.difi.no/iso6523-actorid-upis%3A%3A9908%3A9854323/"));
    }

    @Test(enabled = false, expectedExceptions = TryAgainLaterException.class)
    public void test503() throws Exception {
        URL mock = EasyMock.createMock(URL.class);
        HttpURLConnection httpURLConnection = EasyMock.createMock(HttpURLConnection.class);
        EasyMock.expect(mock.openConnection()).andReturn(httpURLConnection);
        httpURLConnection.connect();
        EasyMock.expect(httpURLConnection.getContentEncoding()).andReturn("");
        EasyMock.expect(httpURLConnection.getResponseCode()).andReturn(503);
        EasyMock.expect(httpURLConnection.getHeaderField("Retry-After")).andReturn("120");

        EasyMock.replay(mock, httpURLConnection);
        new SmpContentRetrieverImpl().getUrlContent(mock);
    }


}
