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

package eu.peppol.persistence;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolMessageMetaData;
import eu.peppol.identifier.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Principal;
import java.util.Date;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Steinar Overbeck Cook
 * @author Thore Johnsen
 */
public class SimpleMessageRepositoryTest {

    SimpleMessageRepository simpleMessageRepository;
    private Path tempDirectory;

    @BeforeMethod
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("UNIT");
        simpleMessageRepository = new SimpleMessageRepository(tempDirectory.toFile());
    }

    @AfterMethod
    public void removeTempDirAndAllFiles() throws IOException {

        Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });

    }


    @Test
    public void verifyFileNameNormalization() {

        // reserved in windows : <>:"/|\?*
        assertEquals(SimpleMessageRepository.normalizeFilename(
            "Reserved<>:\"/|\\?*Windows"),
            "Reserved_________Windows");

        // never end with "\t" tab
        assertEquals(SimpleMessageRepository.normalizeFilename(
            "No\tTab\tAt\tEnd\t"),
            "No_Tab_At_End_");

        // never include or end in " " space
        assertEquals(SimpleMessageRepository.normalizeFilename(
            "No Space Any Where "),
            "No_Space_Any_Where_");

        // just some random combination to assert - and . are still allowed
        assertEquals(SimpleMessageRepository.normalizeFilename(
            "Crazy<File.xml>Name@With¨Loads/Of\\Il-legal´Chars\t"),
            "Crazy_File.xml_Name_With_Loads_Of_Il-legal_Chars_");

    }


    @Test
    public void computeDirectoryNameForMessage() throws IOException {

        ParticipantId recipientId = new ParticipantId("9908:976098897");
        ParticipantId senderId = new ParticipantId("9908:123456789");

        String tmpdir = "/tmpx";

        File dirName = simpleMessageRepository.computeDirectoryNameForInboundMessage(tmpdir, recipientId, senderId);
        
        assertEquals(dirName, new File(tmpdir + "/9908_976098897/9908_123456789"), "Invalid directory name computed");
    }

    @Test
    public void computeDirectoryNameForMessageWithNoChannel() throws IOException {

        ParticipantId recipientId = new ParticipantId("9908:976098897");
        ParticipantId senderId = new ParticipantId("9908:123456789");


        String tmpdir = "/tmpx";

        File dirName = simpleMessageRepository.computeDirectoryNameForInboundMessage(tmpdir, recipientId, senderId);
        assertEquals(dirName, new File(tmpdir + "/9908_976098897/9908_123456789"), "Invalid directory name computed");
    }

    @Test
    public void testPrepareMessageStore() {

        File tmpDir = new File(System.getProperty("java.io.tmpdir"));

        File tmp = new File(tmpDir, "/X");
        try {
            tmp.mkdirs();
            MessageId messageId = new MessageId("uuid:c5aa916d-9a1e-4ae8-ba25-0709ec913acb");
            ParticipantId recipientId = new ParticipantId("9908:976098897");
            ParticipantId senderId = new ParticipantId("9908:123456789");

            simpleMessageRepository.prepareMessageDirectory(tmp.toString(),recipientId, senderId);
        } finally {
            tmp.delete();
        }
    }

    @Test
    public void verifyFullHeadersAsJSON() {

        PeppolMessageMetaData metadata = new PeppolMessageMetaData();
        metadata.setMessageId(new MessageId(UUID.randomUUID().toString()));
        metadata.setRecipientId(new ParticipantId("9908:976098897"));
        metadata.setSenderId(new ParticipantId("9908:976098897"));
        metadata.setDocumentTypeIdentifier(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        metadata.setProfileTypeIdentifier(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId());
        metadata.setSendingAccessPoint(new AccessPointIdentifier("XX_9876543210"));
        metadata.setReceivingAccessPoint(new AccessPointIdentifier("YY_0123456789"));
        metadata.setProtocol(BusDoxProtocol.AS2);
        metadata.setUserAgent("IDEA-Agent");
        metadata.setUserAgentVersion("v9");
        metadata.setSendersTimeStamp(new Date());
        metadata.setReceivedTimeStamp(new Date());
        metadata.setSendingAccessPointPrincipal(createPrincipal());
        metadata.setTransmissionId(new TransmissionId());

        String jsonString = simpleMessageRepository.getHeadersAsJSON(metadata);

        try {
            JsonParser parser = new JsonParser();
            parser.parse(jsonString);
        } catch (JsonSyntaxException ex) {
            fail("Illegal JSON produced : " + jsonString);
        }
    }

    @Test
    public void verifyEmptyHeadersAsJSON() {

        PeppolMessageMetaData metadata = new PeppolMessageMetaData();
        // no values set, most should be "null", validate that we still has valid JSON

        String jsonString = simpleMessageRepository.getHeadersAsJSON(metadata);

        try {
            JsonParser parser = new JsonParser();
            parser.parse(jsonString);
        } catch (JsonSyntaxException ex) {
            fail("Illegal JSON produced : " + jsonString);
        }

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
