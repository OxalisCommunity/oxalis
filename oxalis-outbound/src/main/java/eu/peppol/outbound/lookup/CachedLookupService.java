package eu.peppol.outbound.lookup;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.vefa.peppol.common.model.*;
import no.difi.vefa.peppol.lookup.LookupClient;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author erlend
 * @since 4.0.0
 */
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
            throw new OxalisTransmissionException(e.getMessage(), e);
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

            if (!receiver.equals(that.receiver)) return false;
            if (!documentType.equals(that.documentType)) return false;
            return process.equals(that.process);

        }

        @Override
        public int hashCode() {
            int result = receiver.hashCode();
            result = 31 * result + documentType.hashCode();
            result = 31 * result + process.hashCode();
            return result;
        }
    }
}
