/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.smp;

import com.google.inject.Inject;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.RuntimeConfigurationModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 17.12.13
 *         Time: 23:24
 */
@Guice(modules = {RuntimeConfigurationModule.class})
public class SmpModuleTest {

    @Inject
    SmpLookupManager smpLookupManager;

    @Inject
    GlobalConfiguration g1;

    @Inject GlobalConfiguration g2;

    @Test
    public void verifySmpModule() throws Exception {
        assertNotNull(smpLookupManager);

        // Ensures that Google Guice takes care of the singleton scope.
        assertEquals(g1, g2);
    }
}
