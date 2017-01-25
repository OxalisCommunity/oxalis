/*
 * Copyright (c) 2010 - 2017 Norwegian Agency for Public Government and eGovernment (Difi)
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

package no.difi.oxalis.commons.plugin;

import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolProcessTypeIdAcronym;
import eu.peppol.identifier.WellKnownParticipant;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.vefa.peppol.common.model.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 24.01.2017
 *         Time: 09.38
 */
public class PayloadPersisterProviderTest {

    private PayloadPersister payloadPersister = (mi, h, in) -> null;

    private Path lib;

    @BeforeMethod
    public void setUp() throws Exception {
        String property = System.getProperty("java.home");
        lib = Paths.get(property, "lib");
    }

    @Test
    public void testFindJarFiles() throws Exception {

        PayloadPersisterProvider payloadPersisterProvider = new PayloadPersisterProvider(lib, payloadPersister);
        URL[] jarFiles = payloadPersisterProvider.findJarFiles(lib);
        assertTrue(jarFiles.length > 0);
    }


    @Test
    public void loadDefaultPersister() throws Exception {
        PayloadPersisterProvider payloadPersisterProvider = new PayloadPersisterProvider(lib, payloadPersister);
        PayloadPersister payloadPersister = payloadPersisterProvider.get();
        assertTrue(payloadPersister == payloadPersister, "Expected an instance of the default persister");
    }

    @Test
    public void loadImplementationFromSpiralis() throws Exception {

        // This test will only work on Steinar Cook's machine :-)
        if ("steinar".equals(System.getProperty("user.name"))) {
            Path path = Paths.get("/Users/steinar/src/spiralis/oxalis-plugin/target");
            PayloadPersisterProvider payloadPersisterProvider = new PayloadPersisterProvider(path, payloadPersister);
            PayloadPersister payloadPersister = payloadPersisterProvider.get();
            assertFalse(payloadPersister == payloadPersister, "Ooops, did not expect an instance of default " + PayloadPersister.class.getCanonicalName());
            System.out.println("Loaded custom " + PayloadPersister.class.getCanonicalName() + " implementation " + payloadPersister.getClass().getCanonicalName());


            ParticipantIdentifier receiver = ParticipantIdentifier.of(WellKnownParticipant.DIFI_TEST.stringValue());
            ParticipantIdentifier sender = ParticipantIdentifier.of("9908:976098897");
            ProcessIdentifier processIdentifier = ProcessIdentifier.of(PeppolProcessTypeIdAcronym.INVOICE_ONLY.toString());
            DocumentTypeIdentifier documentTypeIdentifier = DocumentTypeIdentifier.of(PeppolDocumentTypeIdAcronym.EHF_INVOICE.toString());

            InstanceIdentifier instanceIdentifier = InstanceIdentifier.generateUUID();
            Header header = Header.of(sender, receiver, processIdentifier, documentTypeIdentifier, instanceIdentifier, InstanceType.of("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2", "Invoice", "2.0"), new Date());

            ByteArrayInputStream inputStream = new ByteArrayInputStream("Hello world".getBytes());
            Path persist = payloadPersister.persist(new MessageId(), Header.newInstance(), inputStream);
        }
    }
}