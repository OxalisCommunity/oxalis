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

package eu.peppol.outbound.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: nigel
 * Date: Oct 8, 2011
 * Time: 11:11:06 AM
 */
public class Log {

    private static Logger log = LoggerFactory.getLogger("oxalis-out");

    public static void error(String s, Throwable throwable) {
        log.error(s, throwable);
    }

    public static void debug(String s) {
        log.debug(s);
    }

    public static void error(String s) {
        log.error(s);
    }

    public static void info(String s) {
        log.info(s);
    }

    public static void warn(String s) {
        log.warn(s);
    }
}
