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

package network.oxalis.as2.model;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 * Date: 17.10.13
 * Time: 21:36
 */
public class As2DispositionNotificationOptionsTest {

    @Test
    public void createFromString() {

        As2DispositionNotificationOptions options = As2DispositionNotificationOptions.valueOf("signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        assertEquals(options.getParameters().size(), 2);

        As2DispositionNotificationOptions.Parameter parameter = options.getSignedReceiptProtocol();
        assertNotNull(parameter);

        As2DispositionNotificationOptions.Parameter micAlg = options.getSignedReceiptMicalg();
        assertNotNull(micAlg);

    }

    @Test
    public void testFromRealMendelsonHeader() {

        As2DispositionNotificationOptions options = As2DispositionNotificationOptions.valueOf("disposition-notification-options: signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1, md5");
        assertEquals(options.getParameters().size(), 2);

        As2DispositionNotificationOptions.Parameter parameter = options.getSignedReceiptProtocol();
        assertNotNull(parameter);

        As2DispositionNotificationOptions.Parameter micAlg = options.getSignedReceiptMicalg();
        assertNotNull(micAlg);

        assertEquals(micAlg.getTextValue(), "sha1, md5");

        assertEquals(options.getPreferredSignedReceiptMicAlgorithmName(), "sha1");

    }

    @Test
    public void testSomeExamplesFromCipa() {

        As2DispositionNotificationOptions o1 = As2DispositionNotificationOptions.valueOf("signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1");
        assertNotNull(o1.getSignedReceiptMicalg());
        assertEquals(o1.getSignedReceiptMicalg().getTextValue(), "sha1");

        As2DispositionNotificationOptions o2 = As2DispositionNotificationOptions.valueOf("signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required, sha1");
        assertNotNull(o2.getSignedReceiptMicalg());
        assertEquals(o2.getSignedReceiptMicalg().getTextValue(), "sha1");

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void createFromInvalidString() {
        As2DispositionNotificationOptions.valueOf("signed-receipt-protocol=reXXuired, pkcs7-signature");
    }

    @Test
    public void testToString() {
        As2DispositionNotificationOptions options = As2DispositionNotificationOptions.valueOf("signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        assertEquals(options.toString(), "signed-receipt-protocol=required,pkcs7-signature; signed-receipt-micalg=required,sha1");

        As2DispositionNotificationOptions opt2 = As2DispositionNotificationOptions.valueOf(options.toString());
        assertNotNull(opt2);
        assertEquals(opt2.getSignedReceiptMicalg().getImportance(), As2DispositionNotificationOptions.Importance.REQUIRED);
    }
}
