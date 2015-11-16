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

package eu.peppol.persistence;

import java.util.Date;

/**
 * Represents the receipt to be provided by C2 to C1 and by C3 to C4.
 * <p>
 * I.e. it is a generic structure which is agnostic to the underlying transport infrastructure.
 *
 * @author steinar
 *         Date: 01.11.2015
 *         Time: 21.24
 */
public interface GenericTransportReceipt {

    Date getReceptionTimeStamp();

}
