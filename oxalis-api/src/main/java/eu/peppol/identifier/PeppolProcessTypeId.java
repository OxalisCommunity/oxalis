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

/* Created by steinar on 23.05.12 at 23:09 */
package eu.peppol.identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Represents a type safe PEPPOL Process type identifier.
 *
 * @see "Policy for use of Identifiers, section 5"
 *
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class PeppolProcessTypeId implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(PeppolProcessTypeId.class);

    // See Policy 15 and policy 17
    private static final String scheme = "cenbii-procid-ubl";

    private final String processTypeIdentifer;

    public PeppolProcessTypeId(String processTypeIdentifer) {
        if (!processTypeIdentifer.startsWith("urn:")) {
            // TODO Change to exception when suitable.
            log.info("PEPPOL process type identifier should start with \"urn\"");
        }

        this.processTypeIdentifer = processTypeIdentifer;
    }

    public String getScheme() {
        return scheme;
    }

    @Override
    public String toString() {
        return processTypeIdentifer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeppolProcessTypeId that = (PeppolProcessTypeId) o;

        if (processTypeIdentifer != null ? !processTypeIdentifer.equals(that.processTypeIdentifer) : that.processTypeIdentifer != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return processTypeIdentifer != null ? processTypeIdentifer.hashCode() : 0;
    }


    public static PeppolProcessTypeId valueOf(String processTypeIdentifer) {
        return new PeppolProcessTypeId(processTypeIdentifer);
    }
}
