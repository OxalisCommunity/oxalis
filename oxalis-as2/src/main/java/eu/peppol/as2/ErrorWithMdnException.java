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
