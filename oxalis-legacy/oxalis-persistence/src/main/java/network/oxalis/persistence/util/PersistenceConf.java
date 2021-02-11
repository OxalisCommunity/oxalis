/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package network.oxalis.persistence.util;

import network.oxalis.api.settings.*;

/**
 * @author erlend
 * @since 4.0.0
 */
@Title("Persistence")
public enum PersistenceConf {

    @Path("oxalis.database.datasource")
    @DefaultValue("dbcp")
    DATASOURCE,

    @Path("oxalis.database.driver.class")
    @DefaultValue("org.h2.Driver")
    DRIVER_CLASS,

    @Path("oxalis.database.driver.path")
    @Nullable
    DRIVER_PATH,

    @Path("oxalis.database.jdbc.connection")
    @DefaultValue("jdbc:h2:file:./data/oxalis")
    JDBC_CONNECTION_URI,

    @Path("oxalis.database.jdbc.username")
    @DefaultValue("sa")
    JDBC_USERNAME,

    @Path("oxalis.database.jdbc.password")
    @DefaultValue("")
    @Secret
    JDBC_PASSWORD,

    @Path("oxalis.database.jndi.resource")
    @DefaultValue("jdbc/oxalis")
    JNDI_RESOURCE,

    @Path("oxalis.database.dbcp.max.idle")
    @DefaultValue("30")
    DBCP_MAX_IDLE,

    @Path("oxalis.database.dbcp.max.total")
    @DefaultValue("100")
    DBCP_MAX_TOTAL,

    @Path("oxalis.database.dbcp.validation")
    @DefaultValue("select 1")
    DBCP_VALIDATION_QUERY,

}
