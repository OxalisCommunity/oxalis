/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.persistence.jdbc.util;

import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.persistence.AccountId;
import eu.peppol.persistence.MessageMetaData;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.TransferDirection;
import eu.peppol.persistence.api.UserName;
import eu.peppol.persistence.api.account.Account;
import eu.peppol.persistence.api.account.AccountRepository;
import eu.peppol.persistence.api.account.Customer;
import eu.peppol.persistence.guice.TestModuleFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

/**
 * @author steinar
 *         Date: 11.11.2016
 *         Time: 10.21
 */
@Guice(moduleFactory = TestModuleFactory.class)

public class DatabaseHelperTest {

    @Inject
    DatabaseHelper databaseHelper;

    @Inject
    AccountRepository accountRepository;

    @Inject
    MessageRepository messageRepository;

    private Account account;

    @BeforeMethod
    public void setUp() {
        account = new Account(
                new Customer(1, "Difi test", new Date(), "Steinar Overbeck Cook", "steinar@difi.no", "90665793", "Norway", "Addr1", "Addr2", "1472", "Fjellhamar", "NO810418052"),
                new UserName("steinar"), new Date(), "ringo1", new AccountId(2), false, false
        );

        account = accountRepository.createAccount(this.account, WellKnownParticipant.DIFI_TEST);
        assertNotEquals(account.getId().toInteger(),Integer.valueOf(1));

    }

    /**
     * Ensures that we a) can insert a messag and b) the message is associated with the account we specify, i.e. there is no attempt made
     * to connect it to another account.
     *
     * @throws Exception
     */
    @Test
    public void testCreateMessageVerifyAccount() throws Exception {

        // Creates an outbound message, which should be associated with account no #2
        // even though the receivers ppid is bound to account #1
        Long msgNo = databaseHelper.createMessage(account.getId().toInteger(), TransferDirection.OUT, WellKnownParticipant.DUMMY.stringValue(), WellKnownParticipant.DUMMY.stringValue(), UUID.randomUUID().toString(), new Date());

        MessageMetaData messageByNo = messageRepository.findMessageByNo(msgNo);
        assertEquals(messageByNo.getAccountId().get(), account.getId());
    }

    @Test
    public void testCreateMessage1() throws Exception {

    }

    @Test
    public void testCreateMessage2() throws Exception {

    }

    @Test
    public void testDeleteMessage() throws Exception {

    }

}