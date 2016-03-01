/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
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

package eu.peppol.security.x509;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 21.05.13
 *         Time: 16:16
 */
public class CertificateTest {

    @Test
    public void loadCertificate() throws Exception {

        InputStream inputStream = CertificateTest.class.getClassLoader().getResourceAsStream("unit4-accesspoint.cer");
        if (inputStream == null) {
            throw new IllegalStateException("Unable to find SR certificate");
        }

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        Certificate certificate = certificateFactory.generateCertificate(inputStream);
        assertNotNull(certificate,"No certificate generated");
        assertEquals(certificate.getType(), "X.509");

        X509Certificate x509Certificate = (X509Certificate) certificate;
        x509Certificate.checkValidity(new Date());

        CertPath certPath = certificateFactory.generateCertPath(Arrays.asList(x509Certificate));
        CertPathValidator certPathValidator = CertPathValidator.getInstance("PKIX");
    }

    /**
     * Creates a X509 V3 certificate using Bouncy Castle
     *
     * @throws NoSuchAlgorithmException
     * @throws OperatorCreationException
     * @throws CertificateException
     * @throws NoSuchProviderException
     */
    @Test(enabled = false)
    public void createSampleCertificate() throws NoSuchAlgorithmException, OperatorCreationException, CertificateException, NoSuchProviderException {

        Security.addProvider(new BouncyCastleProvider());

        KeyPair keyPair = generateKeyPair();

        ContentSigner sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(new BouncyCastleProvider()).build(keyPair.getPrivate());


        Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);

        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

        X509v3CertificateBuilder x509v3CertificateBuilder = new X509v3CertificateBuilder(new X500Name("CN=AP_UNIT_TEST"), BigInteger.ONE, startDate, endDate, new X500Name("CN=AP_UNIT_TEST"), subjectPublicKeyInfo);
        X509CertificateHolder x509CertificateHolder = x509v3CertificateBuilder.build(sigGen);
        X509Certificate cert = new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(x509CertificateHolder);

    }

    protected KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();

        keyPairGenerator.initialize(2048, secureRandom);
        return keyPairGenerator.generateKeyPair();
    }
}
