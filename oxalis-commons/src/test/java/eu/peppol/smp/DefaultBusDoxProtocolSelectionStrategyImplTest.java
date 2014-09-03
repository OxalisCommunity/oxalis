/*
 * Copyright (c) 2011,2012,2013,2014 UNIT4 Agresso AS.
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

import eu.peppol.BusDoxProtocol;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 02.01.14
 *         Time: 13:09
 */
public class DefaultBusDoxProtocolSelectionStrategyImplTest {

    private DefaultBusDoxProtocolSelectionStrategyImpl strategy;

    @BeforeMethod
    public void setUp() {
        strategy = new DefaultBusDoxProtocolSelectionStrategyImpl();
    }

    @Test
    public void verifyAllProtocols() throws Exception {
        for (BusDoxProtocol busDoxProtocol : BusDoxProtocol.values()) {
            List<BusDoxProtocol> busDoxProtocols = Arrays.asList(busDoxProtocol);
            BusDoxProtocol selectedProtocol = strategy.selectOptimalProtocol(busDoxProtocols);
            assertNotNull(selectedProtocol);
        }
    }

    @Test
    public void selectFromList() throws Exception {
        List<BusDoxProtocol> acceptedProtocols = Arrays.asList(BusDoxProtocol.START, BusDoxProtocol.AS2);
        BusDoxProtocol selectedProtocol = strategy.selectOptimalProtocol(acceptedProtocols);
        assertEquals(selectedProtocol, BusDoxProtocol.AS2);
    }

}
