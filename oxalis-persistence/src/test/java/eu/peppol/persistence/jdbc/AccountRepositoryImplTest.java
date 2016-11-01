package eu.peppol.persistence.jdbc;

import com.google.inject.Inject;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.persistence.ObjectMother;
import eu.peppol.persistence.TransferDirection;
import eu.peppol.persistence.api.MessageNumber;
import eu.peppol.persistence.api.SrAccountNotFoundException;
import eu.peppol.persistence.api.UserName;
import eu.peppol.persistence.api.account.Account;
import eu.peppol.persistence.api.account.AccountId;
import eu.peppol.persistence.api.account.AccountRepository;
import eu.peppol.persistence.api.account.Customer;
import eu.peppol.persistence.guice.TestModuleFactory;
import eu.peppol.persistence.jdbc.util.DatabaseHelper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

/**
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 31.12.11
 *         Time: 17:24
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class AccountRepositoryImplTest {


    Account adamsAccount;
    private AccountRepository accountRepository;
    private Account ringoAccount;
    private ParticipantId participantId;
    private DatabaseHelper databaseHelper;
    private Customer customer;

    @Inject
    public AccountRepositoryImplTest(DatabaseHelper databaseHelper, AccountRepository accountRepository) {
        this.databaseHelper = databaseHelper;
        this.accountRepository = accountRepository;
    }

    @BeforeMethod(groups = {"persistence"})
    public void setUp() throws Exception {
        participantId = ObjectMother.getAdamsParticipantId();
        adamsAccount = ObjectMother.getAdamsAccount();
        adamsAccount = accountRepository.createAccount(adamsAccount, participantId);
        ringoAccount = accountRepository.createAccount(ringoAccount, participantId);

    }

    @AfterMethod(groups = {"persistence"})
    public void tearDown() throws Exception {
        databaseHelper.deleteAllMessagesForAccount(ringoAccount);
        accountRepository.deleteAccount(ringoAccount.getId());
        databaseHelper.deleteCustomer(customer);

        databaseHelper.deleteAllMessagesForAccount(adamsAccount);
        accountRepository.deleteAccount(adamsAccount.getId());
    }

    @Test(groups = {"persistence"})
    public void testFindAccountById() throws Exception {

        Account accountById = accountRepository.findAccountById(ringoAccount.getId());

        assertNotNull(accountById.getId());
        assertNotNull(accountById.getCustomer());
    }

    @Test(groups = {"persistence"})
    public void testFindAccountByUsername() throws Exception {

        Account accountByUsername = accountRepository.findAccountByUsername(ringoAccount.getUserName());

        assertNotNull(accountByUsername.getId());
        assertNotNull(accountByUsername.getCustomer());
        assertNotNull(accountByUsername.getUserName());
    }


    @Test(groups = {"persistence"})
    public void testFindAccountByParticipantId() throws Exception {
        Account accountByParticipantId = accountRepository.findAccountByParticipantId(participantId);
        assertNotNull(accountByParticipantId);
        assertNotNull(accountByParticipantId.getId());
        assertNotNull(accountByParticipantId.getCustomer());
        assertNotNull(accountByParticipantId.getUserName());
    }

    @Test(groups = {"persistence"})
    public void testAccountExists() throws Exception {
        assertTrue(accountRepository.accountExists(new UserName("sr")));
        assertTrue(accountRepository.accountExists(new UserName("SR")));
        assertFalse(accountRepository.accountExists(new UserName("notExistingAccount")));
    }

    @Test(groups = {"persistence"})
    public void testCreateCustomer() {
        customer = accountRepository.createCustomer("adam", "adam@sendregning.no", "666", "Norge", "Andy S", "Adam vei", "222", "0976", "Oslo", "976098897");
        assertNotNull(customer.getId());
        assertNotNull(customer.getCreated());
        assertEquals("adam", customer.getName());
        assertEquals("adam@sendregning.no", customer.getEmail());
        assertEquals("666", customer.getPhone());
        assertEquals("Norge", customer.getCountry());
        assertEquals("Adam vei", customer.getAddress1());
        assertEquals("222", customer.getAddress2());
        assertEquals("0976", customer.getZip());
        assertEquals("Oslo", customer.getCity());
        assertEquals("976098897", customer.getOrgNo());


    }

    @Test(groups = {"persistence"})
    public void testUpdatePasswordOnAccount() throws SrAccountNotFoundException {
        AccountId id = new AccountId(1);
        String currentPass = accountRepository.findAccountById(id).getPassword();
        String pass = "testPassword";

        accountRepository.updatePasswordOnAccount(id, pass);
        assertEquals(pass, accountRepository.findAccountById(id).getPassword());

        accountRepository.updatePasswordOnAccount(id, currentPass);
        assertEquals(currentPass, accountRepository.findAccountById(id).getPassword());

    }

    @Test(groups = {"persistence"})
    public void testValidateFlag() throws SrAccountNotFoundException {
        assertFalse(adamsAccount.isValidateUpload());

        databaseHelper.updateValidateFlagOnAccount(adamsAccount.getId(), true);

        adamsAccount = accountRepository.findAccountById(adamsAccount.getId());
        assertTrue(adamsAccount.isValidateUpload());

    }

    @Test(groups = {"persistence"})
    public void findMessageOwner() {
        Long messageNumber = databaseHelper.createMessage(adamsAccount.getId().toInteger(), TransferDirection.IN, participantId.stringValue(), participantId.stringValue(), UUID.randomUUID().toString(), null);
        assertEquals(adamsAccount, accountRepository.findAccountAsOwnerOfMessage(MessageNumber.create(messageNumber)));

    }


}
