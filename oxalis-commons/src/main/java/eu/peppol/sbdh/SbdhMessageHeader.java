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

package eu.peppol.sbdh;

import eu.peppol.identifier.*;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Scope;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

/**
 * Wrapper for SBDH.
 */
public class SbdhMessageHeader implements MessageHeader {

    private StandardBusinessDocumentHeader header;

    public SbdhMessageHeader(StandardBusinessDocumentHeader header) {
        this.header = header;
    }

    @Override
    public ParticipantId getFrom() {
        if (header.getSender().size() == 0)
            return null;
        return new ParticipantId(header.getSender().get(0).getIdentifier().getValue());
    }

    @Override
    public ParticipantId getTo() {
        if (header.getReceiver().size() == 0)
            return null;
        return new ParticipantId(header.getReceiver().get(0).getIdentifier().getValue());
    }

    @Override
    public PeppolDocumentTypeId getDocumentIdentifier() {
        return PeppolDocumentTypeId.valueOf(getScopeValue("DOCUMENTID"));
    }

    @Override
    public MessageId getInstanceIdentifier() {
        return new MessageId(header.getDocumentIdentification().getInstanceIdentifier());
    }

    @Override
    public PeppolProcessTypeId getProcessIdentifier() {
        return PeppolProcessTypeId.valueOf(getScopeValue("PROCESSID"));
    }

    private String getScopeValue(String scopeType) {
        for (Scope scope : header.getBusinessScope().getScope()) {
            if (scopeType.equals(scope.getType()))
                return scope.getInstanceIdentifier();
        }

        return null;
    }
}
