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

package network.oxalis.commons.mode;

import com.google.inject.Inject;
import network.oxalis.commons.guice.GuiceModuleLoader;
import network.oxalis.vefa.peppol.common.code.Service;
import network.oxalis.vefa.peppol.security.api.CertificateValidator;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.security.cert.X509Certificate;

@Guice(modules = {GuiceModuleLoader.class})
public class ModeModuleTest {

    @Inject
    private CertificateValidator certificateValidator;

    @Inject
    private X509Certificate certificate;

    @Test
    public void simple() throws Exception {
        certificateValidator.validate(Service.AP, certificate);
    }
}
