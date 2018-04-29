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

package no.difi.oxalis.ext.currenthome;

import com.google.inject.multibindings.Multibinder;
import no.difi.oxalis.api.filesystem.HomeDetector;
import no.difi.oxalis.commons.guice.OxalisModule;

/**
 * @author erlend
 */
public class CurrentModule extends OxalisModule {

    @Override
    protected void configure() {
        Multibinder<HomeDetector> multibinder = Multibinder.newSetBinder(binder(), HomeDetector.class);
        multibinder.addBinding().to(CurrentHomeDetector.class);
    }
}
