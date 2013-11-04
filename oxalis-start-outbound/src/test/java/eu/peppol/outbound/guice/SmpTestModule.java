package eu.peppol.outbound.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.outbound.api.MessageSenderFactory;
import eu.peppol.smp.ParticipantNotRegisteredException;
import eu.peppol.smp.SmpLookupException;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.smp.SmpLookupManagerImpl;
import eu.peppol.identifier.PeppolDocumentTypeId;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 29.10.13
 *         Time: 11:42
 */
public class SmpTestModule extends AbstractModule {


    @Override
    protected void configure() {
        bind(MessageSenderFactory.class);
    }


    @Provides
    public SmpLookupManager getSmpLookupManager() {
        return new SmpLookupManager() {

            @Override
            public URL getEndpointAddress(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {
                try {
                    return new URL("http://localhost:8080/oxalis/as2");
                } catch (MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public X509Certificate getEndpointCertificate(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {
                throw new IllegalStateException("not supported yet");
            }

            @Override
            public List<PeppolDocumentTypeId> getServiceGroups(ParticipantId participantId) throws SmpLookupException, ParticipantNotRegisteredException {
                throw new IllegalStateException("Not supported yet.");
            }

            @Override
            public PeppolEndpointData getEndpointData(ParticipantId participantId, PeppolDocumentTypeId documentTypeIdentifier) {
                try {
                    return new PeppolEndpointData(new URL("http://localhost:8080/oxalis/as2"), BusDoxProtocol.AS2);
                } catch (MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }

}
