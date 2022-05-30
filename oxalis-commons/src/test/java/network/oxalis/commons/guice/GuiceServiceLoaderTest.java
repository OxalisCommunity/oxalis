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

package network.oxalis.commons.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import network.oxalis.api.lang.OxalisLoadingException;
import network.oxalis.api.persist.PayloadPersister;
import network.oxalis.api.persist.ReceiptPersister;
import network.oxalis.api.timestamp.TimestampProvider;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author erlend
 */
public class GuiceServiceLoaderTest {

    private GuiceServiceLoader serviceLoader;

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    @BeforeClass
    public void beforeClass() {
        Injector injector = Guice.createInjector();
        serviceLoader = injector.getInstance(GuiceServiceLoader.class);
    }

    @Test
    public void simple() {
        List<TimestampProvider> providers = serviceLoader.load(TimestampProvider.class, classLoader);

        Assert.assertEquals(providers.size(), 1);
        Assert.assertTrue(providers.get(0) instanceof SampleTimestampProvider);
    }

    @Test
    public void noneDetected() {
        Assert.assertEquals(serviceLoader.load(PayloadPersister.class, classLoader).size(), 0);
    }

    @Test(expectedExceptions = OxalisLoadingException.class)
    public void triggerException() {
        serviceLoader.load(ReceiptPersister.class, classLoader);
    }
}
