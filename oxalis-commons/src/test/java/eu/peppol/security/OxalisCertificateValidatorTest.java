/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Public Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.security;

import com.google.inject.Inject;
import eu.peppol.util.OxalisCommonsTestModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 21.12.2015
 *         Time: 13.33
 */
@Guice(modules = {OxalisCommonsTestModule.class})
public class OxalisCertificateValidatorTest {

    @Inject
    OxalisCertificateValidator oxalisCertificateValidator;

    @Inject KeystoreManager keystoreManager;

    @BeforeMethod
    public void setUp() {
        assertNotNull(oxalisCertificateValidator, "oxalisCertificateValidator not bound by Guice");
        assertNotNull(keystoreManager);
    }
    @Test
    public void validateOurDummyCertificate() throws Exception {
        boolean validate = oxalisCertificateValidator.validate(keystoreManager.getOurCertificate());
        assertTrue(validate, "Validation of certificate failed");
    }
}