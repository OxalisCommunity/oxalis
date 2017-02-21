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

package no.difi.oxalis.commons.evidence;

import com.google.inject.*;
import com.google.inject.name.Names;
import no.difi.oxalis.api.evidence.EvidenceFactory;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.commons.settings.SettingsBuilder;

/**
 * @author erlend
 * @since 4.0.0
 */
public class EvidenceModule extends AbstractModule {

    @Override
    protected void configure() {
        SettingsBuilder.with(binder(), EvidenceConf.class);

        bind(Key.get(EvidenceFactory.class, Names.named("rem")))
                .to(RemEvidenceFactory.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    protected EvidenceFactory getEvidenceFactory(Injector injector, Settings<EvidenceConf> settings) {
        return injector.getInstance(Key.get(EvidenceFactory.class, settings.getNamed(EvidenceConf.SERVICE)));
    }
}
