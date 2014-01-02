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

import java.util.List;

/**
 * @author steinar
 *         Date: 02.01.14
 *         Time: 13:29
 */
public interface BusDoxProtocolSelectionStrategy {

    /**
     * Selects the preferred BusDoxProtocol from the list of accepted protocols
     *
     * @param protocolsAccepted list of protocols accepted by somebody
     * @return the preferred protocol.
     */
    BusDoxProtocol selectOptimalProtocol(List<BusDoxProtocol> protocolsAccepted);
}
