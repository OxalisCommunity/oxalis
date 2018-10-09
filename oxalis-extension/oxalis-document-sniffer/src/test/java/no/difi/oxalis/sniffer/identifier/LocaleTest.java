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

package no.difi.oxalis.sniffer.identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Locale;

/**
 * @author steinar
 * Date: 09.11.2016
 * Time: 18.09
 */
public class LocaleTest {

    private static Logger logger = LoggerFactory.getLogger(LocaleTest.class);

    @Test
    public void whatIsDefaultLocale() {
        Locale aDefault = Locale.getDefault();
        logger.info("Default locale, country: " + aDefault.getCountry());
    }

    @Test
    public void test() {
        String[] strings = {"NO:ORGNR", "DUNS"};
        for (String s : strings) {
            String[] split = s.split(":");
            logger.info(split[0]);
        }
    }
}
