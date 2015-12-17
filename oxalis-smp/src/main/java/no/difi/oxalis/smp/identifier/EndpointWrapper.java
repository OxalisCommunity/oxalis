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

package no.difi.oxalis.smp.identifier;

import no.difi.vefa.peppol.common.model.Endpoint;

import java.net.URI;
import java.security.cert.X509Certificate;

/**
 * Wrapper making Endpoint representation in VEFA library available to Oxalis.
 */
public class EndpointWrapper implements eu.peppol.identifier.Endpoint {

    /**
     * Endpoint information fetched from SMP.
     */
    private Endpoint endpoint;

    public EndpointWrapper(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTransportProfile() {
        return endpoint.getTransportProfile().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getAddress() {
        return URI.create(endpoint.getAddress());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public X509Certificate getCertificate() {
        return endpoint.getCertificate();
    }
}
