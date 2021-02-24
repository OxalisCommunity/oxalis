package network.oxalis.test.asd;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import network.oxalis.api.header.HeaderParser;
import network.oxalis.api.lang.OxalisContentException;
import network.oxalis.api.lang.TimestampException;
import network.oxalis.api.model.Direction;
import network.oxalis.api.model.TransmissionIdentifier;
import network.oxalis.api.persist.PersisterHandler;
import network.oxalis.api.timestamp.Timestamp;
import network.oxalis.api.timestamp.TimestampProvider;
import network.oxalis.vefa.peppol.common.model.Header;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author erlend
 */
@Singleton
public class AsdServlet extends HttpServlet {

    @Inject
    private Provider<PersisterHandler> persisterHandlerProvider;

    @Inject
    private TimestampProvider timestampProvider;

    @Inject
    private HeaderParser headerParser;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().write("Hello ASD world!");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PersisterHandler persisterHandler = persisterHandlerProvider.get();

        TransmissionIdentifier transmissionIdentifier = null;

        Header header = null;

        Path path = null;

        try {
            Timestamp timestamp = timestampProvider.generate(null, Direction.IN);

            byte[] content = ByteStreams.toByteArray(req.getInputStream());

            transmissionIdentifier = TransmissionIdentifier.of(req.getHeader(AsdHeaders.TRANSMISSION_ID));

            header = headerParser.parse(new ByteArrayInputStream(content));

            path = persisterHandler.persist(transmissionIdentifier, header, new ByteArrayInputStream(content));

            persisterHandler.persist(new AsdInboundMetadata(transmissionIdentifier, header, timestamp.getDate()), path);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setHeader(AsdHeaders.STATUS, "OK");
            resp.setHeader(AsdHeaders.TIMESTAMP, timestamp.getDate().toString());
        } catch (TimestampException | OxalisContentException e) {
            persisterHandler.persist(transmissionIdentifier, header, path, e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setHeader(AsdHeaders.STATUS, String.format("ERROR: %s", e.getMessage()));
        }
    }
}
