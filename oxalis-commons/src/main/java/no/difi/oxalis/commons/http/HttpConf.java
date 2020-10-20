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

package no.difi.oxalis.commons.http;

import no.difi.oxalis.api.settings.DefaultValue;
import no.difi.oxalis.api.settings.Path;
import no.difi.oxalis.api.settings.Title;

/**
 * @author erlend
 * @since 4.0.0
 */
@Title("HTTP")
public enum HttpConf {

    @Path("oxalis.http.pool.total")
    @DefaultValue("20")
    POOL_TOTAL,

    @Path("oxalis.http.pool.max_route")
    @DefaultValue("2")
    POOL_MAX_ROUTE,

    @Path("oxalis.http.pool.validate_after_inactivity")
    @DefaultValue("1000")
    POOL_VALIDATE_AFTER_INACTIVITY,

    @Path("oxalis.http.pool.time_to_live")
    @DefaultValue("30")
    POOL_TIME_TO_LIVE,

    @Path("oxalis.http.timeout.connect")
    @DefaultValue("0")
    TIMEOUT_CONNECT,

    @Path("oxalis.http.timeout.read")
    @DefaultValue("0")
    TIMEOUT_READ,

    @Path("oxalis.http.timeout.socket")
    @DefaultValue("0")
    TIMEOUT_SOCKET,
}
