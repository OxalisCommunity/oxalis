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

/* Created by steinar on 01.01.12 at 14:29 */
package eu.peppol.persistence.api;

/**
 * Value object representing username
 *
 * @author adam
 */
public class UserName {
    private final String username;

    public UserName(String username) {
        if (username == null) {
            throw new IllegalArgumentException("username required");
        }
        this.username = username.toLowerCase();
    }


    public String stringValue() {
        return username.toString();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("UserName");
        sb.append("{username='").append(username).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserName userName = (UserName) o;

        if (username != null ? !username.equals(userName.username) : userName.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}
