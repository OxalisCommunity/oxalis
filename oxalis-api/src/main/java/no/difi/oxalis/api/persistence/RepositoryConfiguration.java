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

package no.difi.oxalis.api.persistence;

import java.net.URI;
import java.nio.file.Path;

/**
 * Holds the configuration parameters required for creating a JDBC connection or DataSource.
 *
 *
 * @author steinar
 *         Date: 27.10.2016
 *         Time: 13.07
 *
 */
public interface RepositoryConfiguration {

    // TODO: refactor this as it is only used for persistence of payload data. It violates the coherence of this
    // interface.
    String BASE_PATH_NAME = "BaseDir";

    /** Provides the {@link java.nio.file.Path} to the root directory of the file based message repository */
    Path getBasePath();

    URI getJdbcConnectionUri();

    String getJdbcDriverClassPath();

    String getJdbcDriverClassName();

    String getJdbcUsername();

    String getJdbcPassword();

    String getValidationQuery();
}
