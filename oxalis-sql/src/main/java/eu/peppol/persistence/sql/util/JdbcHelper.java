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

package eu.peppol.persistence.sql.util;

import java.util.Calendar;
import java.util.Date;

/**
 * @author steinar
 *         Date: 15.08.13
 *         Time: 16:13
 */
public class JdbcHelper {

    public static Date setEndDateIfNull(java.util.Date end) {
        if (end == null) {
            end = new java.util.Date();
        }
        return end;
    }

    public static Date setStartDateIfNull(java.util.Date start) {
        Date result = start;
        if (start == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2013, Calendar.FEBRUARY, 1);
            result = calendar.getTime();
        }

        return result;
    }
}
