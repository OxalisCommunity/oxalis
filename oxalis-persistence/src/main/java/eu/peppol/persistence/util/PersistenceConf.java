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

package eu.peppol.persistence.util;

import no.difi.oxalis.commons.config.builder.DefaultValue;
import no.difi.oxalis.commons.config.builder.Nullable;
import no.difi.oxalis.commons.config.builder.Path;
import no.difi.oxalis.commons.config.builder.Title;

/**
 * @author erlend
 * @since 4.0.0
 */
@Title("Persistence")
public enum PersistenceConf {

    @Path("oxalis.database.datasource")
    @DefaultValue("dbcp")
    DATASOURCE,

    @Path("oxalis.jdbc.driver.class")
    DRIVER_CLASS,

    @Path("oxalis.jdbc.class.path")
    @Nullable
    DRIVER_PATH,

    @Path("oxalis.jdbc.connection.uri")
    JDBC_CONNECTION_URI,

    @Path("oxalis.jdbc.user")
    @DefaultValue("sa")
    JDBC_USERNAME,

    @Path("oxalis.jdbc.password")
    @DefaultValue("")
    JDBC_PASSWORD,

    @Path("oxalis.jdbc.validation.query")
    @DefaultValue("select 1")
    POOL_VALIDATION_QUERY,

    @Path("oxalis.datasource.jndi.name")
    @DefaultValue("jdbc/oxalis")
    JNDI_RESOURCE,

}
