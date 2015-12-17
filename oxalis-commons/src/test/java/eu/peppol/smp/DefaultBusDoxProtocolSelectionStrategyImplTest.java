/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
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
        List<BusDoxProtocol> acceptedProtocols = Arrays.asList(BusDoxProtocol.AS2);
        BusDoxProtocol selectedProtocol = strategy.selectOptimalProtocol(acceptedProtocols);
        assertEquals(selectedProtocol, BusDoxProtocol.AS2);
    }

}
