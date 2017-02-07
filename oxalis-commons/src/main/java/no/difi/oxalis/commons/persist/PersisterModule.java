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

package no.difi.oxalis.commons.persist;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.api.persist.ReceiptPersister;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.commons.plugin.PluginFactory;
import no.difi.oxalis.commons.settings.SettingsBuilder;

/**
 * @author erlend
 * @since 4.0.0
 */
public class PersisterModule extends AbstractModule {

    @Override
    protected void configure() {
        SettingsBuilder.with(binder(), PersisterConf.class);

        // Default
        bind(Key.get(PayloadPersister.class, Names.named("default")))
                .to(DefaultPersister.class)
                .in(Singleton.class);
        bind(Key.get(ReceiptPersister.class, Names.named("default")))
                .to(DefaultPersister.class)
                .in(Singleton.class);

        // Noop
        bind(Key.get(PayloadPersister.class, Names.named("noop")))
                .to(NoopPersister.class)
                .in(Singleton.class);
        bind(Key.get(ReceiptPersister.class, Names.named("noop")))
                .to(NoopPersister.class)
                .in(Singleton.class);

        // Temp
        bind(Key.get(PayloadPersister.class, Names.named("temp")))
                .to(TempPersister.class)
                .in(Singleton.class);
        bind(Key.get(ReceiptPersister.class, Names.named("temp")))
                .to(TempPersister.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    @Named("plugin")
    protected PayloadPersister getPayloadPersisterMetainf(PluginFactory pluginFactory) {
        return pluginFactory.newInstance(PayloadPersister.class);
    }

    @Provides
    @Singleton
    @Named("plugin")
    protected ReceiptPersister getReceiptPersisterMetainf(PluginFactory pluginFactory) {
        return pluginFactory.newInstance(ReceiptPersister.class);
    }

    @Provides
    @Singleton
    protected PayloadPersister getPayloadPersister(Injector injector, Settings<PersisterConf> settings) {
        return injector.getInstance(Key.get(PayloadPersister.class, settings.getNamed(PersisterConf.PAYLOAD_SERVICE)));
    }

    @Provides
    @Singleton
    protected ReceiptPersister getReceiptPersister(Injector injector, Settings<PersisterConf> settings) {
        return injector.getInstance(Key.get(ReceiptPersister.class, settings.getNamed(PersisterConf.RECEIPT_SERVICE)));
    }
}
