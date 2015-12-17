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

import java.util.Arrays;
import java.util.List;

/**
 * @author steinar
 *         Date: 02.01.14
 *         Time: 12:59
 */
public class DefaultBusDoxProtocolSelectionStrategyImpl implements BusDoxProtocolSelectionStrategy  {

    /**  List of known protocols in <em>priority order</em> */
    List<BusDoxProtocol> knownProtocols = Arrays.asList(BusDoxProtocol.AS2);

    /**
     * Selects the optimal protocol from the supplied list of accepted protocols.
     *
     * @param protocolsAccepted protocols accepted
     * @return the optimal protocol based upon this strategy
     */
    @Override
    public BusDoxProtocol selectOptimalProtocol(List<BusDoxProtocol> protocolsAccepted) {
        int lowestIndex = Integer.MAX_VALUE;
        for (BusDoxProtocol busDoxProtocol : protocolsAccepted) {
            int indexOf = knownProtocols.indexOf(busDoxProtocol);
            if (indexOf == -1) {
                throw new IllegalArgumentException("BusDox protocol " + busDoxProtocol + " not known by protocol selection strategy");
            }
            if (indexOf > -1 && indexOf < lowestIndex) {
                lowestIndex = indexOf;
            }
        }
        return knownProtocols.get(lowestIndex);
    }

}
