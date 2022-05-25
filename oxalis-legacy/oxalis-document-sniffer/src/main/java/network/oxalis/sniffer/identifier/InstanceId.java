/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.sniffer.identifier;

import network.oxalis.vefa.peppol.common.model.InstanceIdentifier;

import java.util.UUID;

/**
 * Represents the Instance Identifier used in the SBDH.
 *
 * @author steinar
 *         Date: 21.11.2016
 *         Time: 09.22
 */
public class InstanceId implements java.io.Serializable {


    private final String value;

    /** Creates new InstanceId with random UUID */
    public InstanceId() {
        value = UUID.randomUUID().toString();
    }

    /** Creates new InstanceId with supplied value */
    public InstanceId(String value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return value;
    }

    public InstanceId valueOf(String value) {
        return new InstanceId(value);
    }

    public InstanceIdentifier toVefa() {
        return InstanceIdentifier.of(value);
    }
}
