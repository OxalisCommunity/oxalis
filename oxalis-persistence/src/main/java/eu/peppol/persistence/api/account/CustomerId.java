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

import java.io.Serializable;

public class CustomerId implements Serializable {
    private Integer id;

    public CustomerId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id required");
        }
        this.id = Integer.parseInt(id);
    }

    public CustomerId(Integer id){
        if (id == null) {
            throw new IllegalArgumentException("id required");
        }

        this.id = id;
    }

    public Integer toInteger(){
        return id;
    }


    public String toString() {
        return id.toString();
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomerId that = (CustomerId) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }


    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
