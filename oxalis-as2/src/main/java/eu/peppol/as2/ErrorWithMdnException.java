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

package eu.peppol.as2;

/**
 * Thrown when an error implies that a disposition type of "failed" should be returned rather
 * than "processed".
 *
 * @author steinar
 *         Date: 20.10.13
 *         Time: 11:36
 */
public class ErrorWithMdnException extends Exception {
    private final MdnData mdnData;

    public ErrorWithMdnException(MdnData mdnData) {

        // The error message is contained in the disposition modifier.
        super(mdnData.getAs2Disposition().getDispositionModifier().toString());
        this.mdnData = mdnData;
    }

    public ErrorWithMdnException(MdnData mdnData, Exception e) {
        super(mdnData.getAs2Disposition().getDispositionModifier().toString(), e);
        this.mdnData = mdnData;
    }

    public MdnData getMdnData() {
        return mdnData;
    }
}
