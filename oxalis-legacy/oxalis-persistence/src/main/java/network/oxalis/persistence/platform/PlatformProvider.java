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

package network.oxalis.persistence.platform;

import com.google.inject.Inject;
import com.google.inject.Provider;
import network.oxalis.api.lang.OxalisLoadingException;
import network.oxalis.persistence.annotation.Repository;
import network.oxalis.persistence.api.JdbcTxManager;
import network.oxalis.persistence.api.Platform;

import java.sql.SQLException;
import java.util.Set;

/**
 * @author erlend
 */
@Repository
class PlatformProvider implements Provider<Platform> {

    private JdbcTxManager jdbcTxManager;

    private Set<Platform> platforms;

    @Inject
    public PlatformProvider(JdbcTxManager jdbcTxManager, Set<Platform> platforms) {
        this.jdbcTxManager = jdbcTxManager;
        this.platforms = platforms;
    }

    @Override
    public Platform get() {
        try {
            String productName = jdbcTxManager.getConnection().getMetaData().getDatabaseProductName();

            for (Platform platform : platforms)
                if (platform.detect(productName))
                    return platform;

            throw new OxalisLoadingException(String.format("Unable to load platform for '%s'.", productName));
        } catch (SQLException e) {
            throw new OxalisLoadingException("Unable to detect database platform.", e);
        }
    }
}
