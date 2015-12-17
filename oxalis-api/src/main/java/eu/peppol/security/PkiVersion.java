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

package eu.peppol.security;

/**
 * Known versions of the PKI subsystem.
 *
 * @author steinar
 *         Date: 24.05.13
 *         Time: 16:52
 */
public enum PkiVersion {
    /** Initial version, in which all certificates were "test" certificates, no production certificates */
    V1,
    /** Transitional version only to be used between Sept 1st and Oct 1st 2013 */
    T,
    /** Second version, in which certificates comes in two flavours; TEST or PRODUCTION */
    V2;
}
