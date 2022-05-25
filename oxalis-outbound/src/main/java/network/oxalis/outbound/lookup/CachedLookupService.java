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

package network.oxalis.outbound.lookup;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.lookup.LookupService;
import network.oxalis.api.util.Type;
import network.oxalis.vefa.peppol.common.model.*;
import network.oxalis.vefa.peppol.lookup.LookupClient;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author erlend
 * @since 4.0.0
 */
@Singleton
@Type("cached")
class CachedLookupService extends CacheLoader<CachedLookupService.HeaderStub, Endpoint> implements LookupService {

    private final LookupClient lookupClient;

    private final TransportProfile[] transportProfiles;

    private final LoadingCache<HeaderStub, Endpoint> cache;

    @Inject
    public CachedLookupService(LookupClient lookupClient,
                               @Named("prioritized") List<TransportProfile> transportProfiles) {
        this.lookupClient = lookupClient;
        this.transportProfiles = transportProfiles.toArray(new TransportProfile[transportProfiles.size()]);

        this.cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(this);
    }

    @Override
    public Endpoint lookup(Header header) throws OxalisTransmissionException {
        try {
            return cache.get(new HeaderStub(header));
        } catch (ExecutionException e) {
            throw new OxalisTransmissionException(e.getCause().getMessage(), e.getCause());
        }
    }

    @Override
    public Endpoint load(HeaderStub header) throws Exception {
        return lookupClient.getEndpoint(header.getReceiver(), header.getDocumentType(),
                header.getProcess(), transportProfiles);
    }

    static class HeaderStub {

        private ParticipantIdentifier receiver;

        private DocumentTypeIdentifier documentType;

        private ProcessIdentifier process;

        public HeaderStub(Header header) {
            this.receiver = header.getReceiver();
            this.documentType = header.getDocumentType();
            this.process = header.getProcess();
        }

        public ParticipantIdentifier getReceiver() {
            return receiver;
        }

        public DocumentTypeIdentifier getDocumentType() {
            return documentType;
        }

        public ProcessIdentifier getProcess() {
            return process;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HeaderStub that = (HeaderStub) o;
            return Objects.equals(receiver, that.receiver) &&
                    Objects.equals(documentType, that.documentType) &&
                    Objects.equals(process, that.process);
        }

        @Override
        public int hashCode() {
            return Objects.hash(receiver, documentType, process);
        }
    }
}
