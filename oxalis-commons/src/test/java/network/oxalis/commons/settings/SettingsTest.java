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

package network.oxalis.commons.settings;

import com.google.common.collect.ImmutableMap;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.util.Types;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import network.oxalis.api.settings.Path;
import network.oxalis.api.settings.Settings;
import network.oxalis.api.settings.Title;
import network.oxalis.commons.guice.OxalisModule;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author erlend
 */
public class SettingsTest {

    private Injector injector;

    private Settings<TestConf> settings;

    private static final int INT_VALUE = 200;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public void beforeClass() {
        injector = Guice.createInjector(new OxalisModule() {
            @Override
            protected void configure() {
                SettingsBuilder.with(binder(), TestConf.class);

                bind(Config.class).toInstance(ConfigFactory.parseMap(
                        ImmutableMap.of("test.value", String.valueOf(INT_VALUE))));
            }
        });

        settings = injector.getInstance((Key<Settings<TestConf>>)
                Key.get(Types.newParameterizedType(Settings.class, TestConf.class)));
    }

    @Test
    public void simple() {
        Assert.assertEquals(settings.getInt(TestConf.WITH_VALUE), INT_VALUE);
        Assert.assertEquals(settings.getString(TestConf.WITH_DEFAULT), "Test");
        Assert.assertNull(settings.getString(TestConf.WITH_NULLABLE));
        Assert.assertEquals("Test", settings.getNamed(TestConf.WITH_DEFAULT).value());
    }

    /**
     * Make sure exception is triggered if required configuration is not found.
     */
    @Test(expectedExceptions = CreationException.class)
    public void invalidKey() {
        injector.createChildInjector(new OxalisModule() {
            @Override
            protected void configure() {
                SettingsBuilder.with(binder(), TestWithErrorConf.class);
            }
        });
    }

    @Title("Testing without valid key")
    public enum TestWithErrorConf {

        @Path("oxalis.common.testing.invalid.key")
        KEY

    }
}
