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

package eu.peppol.persistence.api.account;

import java.util.Date;

/**
 * TODO: THIS IS A DTO not a domain object
 * Represents a paying access point customer
 *
 */
public class Customer {
    private final Integer id;
    private final String name;
    private final Date created;
    private final String contactPerson;
    private final String email;
    private final String phone;
    private final String address1;
    private final String address2;
    private final String zip;
    private final String city;
    private final String country;
    private final String orgNo;


    public Customer(Integer id, String name, Date created, String contactPerson, String email, String phone, String country, String address1, String address2, String zip, String city, String orgNo) {
        this.created = created;
        this.name = name;
        this.id = id;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.address1 = address1;
        this.address2 = address2;
        this.zip = zip;
        this.city = city;
        this.orgNo = orgNo;
    }


    public Integer getId() {
        return id;
    }

    public CustomerId getCustomerId() {
        return new CustomerId(id);
    }

    public String getName() {
        return name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Date getCreated() {
        return created;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getZip() {
        return zip;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getOrgNo() {
        return orgNo;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Customer customer = (Customer) o;

        if (address1 != null ? !address1.equals(customer.address1) : customer.address1 != null) return false;
        if (address2 != null ? !address2.equals(customer.address2) : customer.address2 != null) return false;
        if (city != null ? !city.equals(customer.city) : customer.city != null) return false;
        if (contactPerson != null ? !contactPerson.equals(customer.contactPerson) : customer.contactPerson != null)
            return false;
        if (country != null ? !country.equals(customer.country) : customer.country != null) return false;
        if (created != null ? !created.equals(customer.created) : customer.created != null) return false;
        if (email != null ? !email.equals(customer.email) : customer.email != null) return false;
        if (id != null ? !id.equals(customer.id) : customer.id != null) return false;
        if (name != null ? !name.equals(customer.name) : customer.name != null) return false;
        if (orgNo != null ? !orgNo.equals(customer.orgNo) : customer.orgNo != null) return false;
        if (phone != null ? !phone.equals(customer.phone) : customer.phone != null) return false;
        if (zip != null ? !zip.equals(customer.zip) : customer.zip != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (contactPerson != null ? contactPerson.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (address1 != null ? address1.hashCode() : 0);
        result = 31 * result + (address2 != null ? address2.hashCode() : 0);
        result = 31 * result + (zip != null ? zip.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (orgNo != null ? orgNo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", created=" + created +
                ", contactPerson='" + contactPerson + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address1='" + address1 + '\'' +
                ", address2='" + address2 + '\'' +
                ", zip='" + zip + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", orgNo='" + orgNo + '\'' +
                '}';
    }

    /**
     * Returns both address lines concatenated if they exist
     */
    public String getAddress() {
        StringBuffer result = new StringBuffer();

        boolean address1Exists = address1 != null && address1.length() > 0;
        boolean address2Exists = address2 != null && address2.length() > 0;

        if (address1Exists) {
            result.append(address1);
            if (address2Exists) {
                result.append(" ");
            }
        }
        if (address2Exists) {
            result.append(address2);
        }
        return result.toString();
    }
}
