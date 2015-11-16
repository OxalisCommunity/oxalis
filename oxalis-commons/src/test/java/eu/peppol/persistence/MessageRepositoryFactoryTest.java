/*
 * Copyright (c) 2015 Steinar Overbeck Cook
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

/* Created by steinar on 24.06.12 at 22:09 */
package eu.peppol.persistence;

import eu.peppol.util.GlobalConfiguration;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.util.ServiceLoader;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
@Test(groups = "manual")
public class MessageRepositoryFactoryTest {

    /**
     * Verifies that loading the plugable persistence implementation via a custom class loader, actually works
     * as expected.
     */
    @Test
    public void createClassLoader() throws MalformedURLException {

        String persistenceClassPath = GlobalConfiguration.getInstance().getPersistenceClassPath();

        ServiceLoader<MessageRepository> sl = MessageRepositoryFactory.createCustomServiceLoader(persistenceClassPath);
        assertNotNull(sl);

        int i = 0;
        for (MessageRepository messageRepository : sl) {
            i++;
        }
        assertTrue(i > 0, "No implementations found");
    }
}
