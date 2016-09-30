/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.evidence;

import java.io.InputStream;
import java.util.Date;

/**
 * Represents the proof of delivery to be provided by C2 to C1 and by C3 to C4.
 * <p>
 * I.e. it is a generic structure which is agnostic to the underlying transport infrastructure.
 *
 * @author steinar
 *         Date: 01.11.2015
 *         Time: 21.24
 */
public interface TransmissionEvidence {

    Date getReceptionTimeStamp();

    /** Provides an InputStream to the underlying implementation of the evidence, suitable for persistence. */
    InputStream getInputStream();
}
