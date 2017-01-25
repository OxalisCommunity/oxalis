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

package eu.peppol.jdbc;

import javax.sql.DataSource;

/**
 * @author steinar
 *         Date: 18.04.13
 *         Time: 13:43
 */
public interface OxalisDataSourceFactory {

    DataSource getDataSource();

    /** Indicates whether the implementation is the one supplied as part of the Oxalis distribution.
     * Only the one supplied as default part of the distribution should return true, hence the default. */
    default boolean isProvidedWithOxalisDistribution() {
        return false;
    }
}
