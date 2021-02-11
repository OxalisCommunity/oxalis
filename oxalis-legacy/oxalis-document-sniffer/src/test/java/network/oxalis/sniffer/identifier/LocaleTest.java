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

package network.oxalis.sniffer.identifier;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.util.Locale;

/**
 * @author steinar
 * Date: 09.11.2016
 * Time: 18.09
 */
@Slf4j
public class LocaleTest {

    @Test
    public void whatIsDefaultLocale() {
        Locale aDefault = Locale.getDefault();
        log.info("Default locale, country: " + aDefault.getCountry());
    }

    @Test
    public void test() {
        String[] strings = {"NO:ORGNR", "DUNS"};
        for (String s : strings) {
            String[] split = s.split(":");
            log.info(split[0]);
        }
    }
}
