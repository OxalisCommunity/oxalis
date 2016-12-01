package eu.peppol.persistence.api.account;

import eu.peppol.persistence.AccountId;
import eu.peppol.persistence.api.UserName;

import java.util.Date;

/**
 * Represents a Ringo user account
 *
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 31.12.11
 *         Time: 16:57
 */
public class Account {

    private final AccountId accountId;
    /** Foreign key referencing the Customer to which this account belongs */
    private final CustomerId customerId;
    private final String name;
    private final UserName username;
    private final String password;
    private final Date created;

    /* should EHF validation be performed on upload */
    private final boolean validateUpload;
    /* Should error notifications be sent on email */
    private final boolean sendNotification;

    public Account(CustomerId customerId, String name, UserName username, Date created, String password, AccountId accountId, boolean validateUpload, boolean sendNotification) {
        this.customerId = customerId;
        this.name = name;
        this.username = username;
        this.created = created;
        this.password = password;
        this.accountId = accountId;
        this.validateUpload = validateUpload;
        this.sendNotification = sendNotification;
    }

    public Account(CustomerId customerId, UserName username) {
        this.customerId = customerId;
        this.username = username;
        this.name = null;
        this.created = new Date();
        this.password = null;
        this.accountId = null;
        this.validateUpload = false;
        this.sendNotification = true;
    }

    public AccountId getAccountId() {
        return accountId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    /** The name of the account, may hold anything */
    public String getName() {
        return name;
    }

    /** The user name used for authentication together with the password */
    public UserName getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Date getCreated() {
        return created;
    }

    public UserName getUserName() {
        return username;
    }

    public boolean isValidateUpload() {
        return validateUpload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account that = (Account) o;

        if (sendNotification != that.sendNotification) return false;
        if (validateUpload != that.validateUpload) return false;
        if (created != null ? !created.equals(that.created) : that.created != null) return false;
        if (customerId != null ? !customerId.equals(that.customerId) : that.customerId != null) return false;
        if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = customerId != null ? customerId.hashCode() : 0;
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (validateUpload ? 1 : 0);
        result = 31 * result + (sendNotification ? 1 : 0);
        return result;
    }

    public boolean isSendNotification() {

        return sendNotification;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Account{");
        sb.append("id=").append(accountId);
        sb.append(", customerId=").append(customerId);
        sb.append(", password='").append(password).append('\'');
        sb.append(", created=").append(created);
        sb.append(", username=").append(username);
        sb.append(", validateUpload=").append(validateUpload);
        sb.append(", sendNotification=").append(sendNotification);
        sb.append('}');
        return sb.toString();
    }
}
