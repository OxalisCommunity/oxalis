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

package no.difi.oxalis.commons.persist;

import com.google.inject.Inject;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.api.persist.PersisterHandler;
import no.difi.oxalis.api.persist.ReceiptPersister;
import no.difi.oxalis.api.util.Type;
import no.difi.vefa.peppol.common.model.Header;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author erlend
 * @since 4.0.0
 */
@Singleton
@Type("default")
public class DefaultPersisterHandler implements PersisterHandler {

    private PayloadPersister payloadPersister;

    private ReceiptPersister receiptPersister;

    @Inject
    public DefaultPersisterHandler(PayloadPersister payloadPersister, ReceiptPersister receiptPersister) {
        this.payloadPersister = payloadPersister;
        this.receiptPersister = receiptPersister;
    }

    @Override
    public void persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
        receiptPersister.persist(inboundMetadata, payloadPath);
    }

    @Override
    public Path persist(TransmissionIdentifier transmissionIdentifier, Header header, InputStream inputStream)
            throws IOException {
        return payloadPersister.persist(transmissionIdentifier, header, inputStream);
    }
}
