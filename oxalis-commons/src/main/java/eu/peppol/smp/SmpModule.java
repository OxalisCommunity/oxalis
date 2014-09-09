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

import com.google.inject.AbstractModule;

/**
 * @author steinar
 *         Date: 17.12.13
 *         Time: 23:21
 */
public class SmpModule extends AbstractModule{

    @Override
    protected void configure() {
        bind(SmpContentRetriever.class).to(SmpContentRetrieverImpl.class);
        bind(SmpLookupManager.class).to(SmpLookupManagerImpl.class);
        bind(BusDoxProtocolSelectionStrategy.class).to(DefaultBusDoxProtocolSelectionStrategyImpl.class);
    }

}
