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

package eu.peppol.smp;

/**
 * Holds the default values for known SML lookup servers.
 *
 * Can be overridden by using oxalis-global.properties :
 * oxalis.sml.hostname=sml.peppolcentral.org
 *
 * @author ebe
 * @author thjo
 */
public class SmlHost {

    public static final SmlHost PRODUCTION_SML = new SmlHost("edelivery.tech.ec.europa.eu");
    public static final SmlHost TEST_SML = new SmlHost("acc.edelivery.tech.ec.europa.eu");

    private String hostname;

    public static SmlHost valueOf(String hostname) {
        return new SmlHost(hostname);
    }

    public SmlHost(String hostname) {
        this.hostname = hostname;
        if (hostname == null || hostname.trim().length() == 0) {
            throw new IllegalArgumentException("Hostname argument required");
        }
    }

    @Override
    public String toString() {
        return hostname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmlHost smlHost = (SmlHost) o;
        if (!hostname.equals(smlHost.hostname)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return hostname.hashCode();
    }

}
