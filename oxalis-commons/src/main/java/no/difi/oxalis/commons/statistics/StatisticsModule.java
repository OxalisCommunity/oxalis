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

package no.difi.oxalis.commons.statistics;

import com.google.inject.*;
import com.google.inject.name.Names;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.api.statistics.StatisticsService;
import no.difi.oxalis.commons.settings.SettingsBuilder;

public class StatisticsModule extends AbstractModule {

    @Override
    protected void configure() {
        SettingsBuilder.with(binder(), StatisticsConf.class);

        bind(Key.get(StatisticsService.class, Names.named("noop")))
                .to(NoopStatisticsService.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    StatisticsService getStatisticsService(Injector injector, Settings<StatisticsConf> settings) {
        return injector.getInstance(
                Key.get(StatisticsService.class, settings.getNamed(StatisticsConf.SERVICE)));
    }
}
