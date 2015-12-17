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

package eu.peppol.document;

/**
 * One should normally not peek into the contents of the payload being transported. However, in order
 * to make things a little user friendly, we need to perform certain parsing operations in order to
 * manage the StandardBusinessDocumentHeader (SBDH).
 *
 * @author steinar
 *         Date: 18.06.15
 *         Time: 16.04
 *
 * @deprecated it is not the responsibility of the access point to inspect the contents.
 */
public interface DocumentSniffer {
    boolean isSbdhDetected();
}
