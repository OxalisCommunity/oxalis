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

    private final AccountId id;
    private final Customer customer;
    private final String password;
    private final Date created;
    private final UserName username;

    /* should EHF validation be performed upon upload */
    private final boolean validateUpload;
    /* Should error notifications be sent on email */
    private final boolean sendNotification;

    // TODO: This is absolutely lame, only the customer.id should be required!!!
    public Account(Customer customer, UserName username, Date created, String password, AccountId id, boolean validateUpload, boolean sendNotification) {
        this.customer = customer;
        this.username = username;
        this.created = created;
        this.password = password;
        this.id = id;
        this.validateUpload = validateUpload;
        this.sendNotification = sendNotification;
    }

    public Account(Customer customer, UserName username) {
        this.customer = customer;
        this.username = username;
        this.created = null;
        this.password = null;
        this.id = null;
        this.validateUpload = false;
        this.sendNotification = true;
    }

    public Customer getCustomer() {
        return customer;
    }

    public AccountId getId() {
        return id;
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
        if (customer != null ? !customer.equals(that.customer) : that.customer != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = customer != null ? customer.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
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
        return "Account{" +
                "customer=" + customer +
                ", id=" + id +
                ", password='" + password + '\'' +
                ", created=" + created +
                ", username=" + username +
                ", validateUpload=" + validateUpload +
                ", sendNotification=" + sendNotification +
                '}';
    }

}
