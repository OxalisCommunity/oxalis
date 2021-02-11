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

package network.oxalis.commons.transformer;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import network.oxalis.api.settings.Settings;
import network.oxalis.api.transformer.ContentDetector;
import network.oxalis.api.transformer.ContentWrapper;
import network.oxalis.commons.guice.ImplLoader;
import network.oxalis.commons.guice.OxalisModule;

/**
 * @author erlend
 * @since 4.0.1
 */
public class TransformerModule extends OxalisModule {

    @Override
    protected void configure() {
        bindTyped(ContentDetector.class, NoopContentDetector.class);
        bindTyped(ContentWrapper.class, NoopContentWrapper.class);

        bindSettings(TransformerConf.class);
    }

    @Provides
    @Singleton
    protected ContentDetector getContentDetector(Injector injector, Settings<TransformerConf> settings) {
        return ImplLoader.get(injector, ContentDetector.class, settings, TransformerConf.DETECTOR);
    }

    @Provides
    @Singleton
    protected ContentWrapper getContentWrapper(Injector injector, Settings<TransformerConf> settings) {
        return ImplLoader.get(injector, ContentWrapper.class, settings, TransformerConf.WRAPPER);
    }
}
