/*
 * Copyright (c) 2011,2012,2013,2015 UNIT4 Agresso AS.
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

package eu.peppol.document;

/**
 * One should normally not peek into the contents of the payload being transported. However, in order
 * to make things a little user friendly, we need to perform certain parsing operations in order to
 * manage the StandardBusinessDocumentHeader (SBDH).
 *
 * @author steinar
 *         Date: 18.06.15
 *         Time: 16.04
 */
public interface DocumentSniffer {
    boolean isSbdhDetected();
}
