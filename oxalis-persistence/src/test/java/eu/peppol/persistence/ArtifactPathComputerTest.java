/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package eu.peppol.persistence;

import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.persistence.file.ArtifactPathComputer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author steinar
 *         Date: 17.10.2016
 *         Time: 11.54
 */
public class ArtifactPathComputerTest {

    public static final Logger log = LoggerFactory.getLogger(ArtifactPathComputerTest.class);

    @Test
    public void testDocumentPath() throws Exception {
        ArtifactPathComputer a = new ArtifactPathComputer(Paths.get("/tmp"));

        Path path = a.createPayloadPathFrom(sampleMetadata());

        log.debug(path.toUri().toString());
    }

    @Test
    public void testNativeReceiptPath() throws Exception {

    }

    @Test
    public void testNormalizeFilename() throws Exception {

    }

    ArtifactPathComputer.FileRepoKey sampleMetadata() {
        UUID uuid = UUID.randomUUID();
        ArtifactPathComputer.FileRepoKey fileRepoKey = new ArtifactPathComputer.FileRepoKey(TransferDirection.IN, new MessageId(), new ParticipantId("9908:976098897"),new ParticipantId("9908:976098897"),LocalDateTime.now());
        return fileRepoKey;
    }

    private Principal createPrincipal() {
        return new Principal() {
            @Override
            public String getName() {
                return "SOME_AP_PRINCIPAL";
            }
        };
    }

}