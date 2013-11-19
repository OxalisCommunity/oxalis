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

package eu.peppol.as2;

import javax.mail.internet.InternetHeaders;

/**
 * @author steinar
 *         Date: 18.11.13
 *         Time: 15:56
 */
public class HeaderUtil {

    public static String getFirstValue(InternetHeaders internetHeaders, String httpHeaderName) {
        String[] value = internetHeaders.getHeader(httpHeaderName);
        if (value == null) {
            return null;
        } else
            return value[0];  //To change body of created methods use File | Settings | File Templates.
    }

}
