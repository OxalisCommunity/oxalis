package eu.peppol.as2;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 17.10.13
 *         Time: 21:36
 */
public class As2DispositionNotificationOptionsTest {

    @Test
    public void createFromString() throws Exception {

        As2DispositionNotificationOptions options = As2DispositionNotificationOptions.valueOf("signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        assertEquals(options.getParameterList().size(), 2);

        As2DispositionNotificationOptions.Parameter  parameter = options.getSignedReceiptProtocol();
        assertNotNull(parameter);

        As2DispositionNotificationOptions.Parameter micAlg = options.getSignedReceiptMicalg();
        assertNotNull(micAlg);

    }

    @Test
    public void testFromRealMendelsonHeader() throws Exception {

        As2DispositionNotificationOptions options = As2DispositionNotificationOptions.valueOf("disposition-notification-options: signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1, md5");
        assertEquals(options.getParameterList().size(), 2);

        As2DispositionNotificationOptions.Parameter parameter = options.getSignedReceiptProtocol();
        assertNotNull(parameter);

        As2DispositionNotificationOptions.Parameter micAlg = options.getSignedReceiptMicalg();
        assertNotNull(micAlg);

        assertEquals(micAlg.getTextValue(), "sha1, md5");

        assertEquals(options.getPreferredSignedReceiptMicAlgorithmName(), "sha1");

    }

    @Test
    public void testSomeExamplesFromCipa() throws Exception {

        As2DispositionNotificationOptions o1 = As2DispositionNotificationOptions.valueOf("signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1");
        assertNotNull(o1.getSignedReceiptMicalg());
        assertEquals(o1.getSignedReceiptMicalg().getTextValue(), "sha1");

        As2DispositionNotificationOptions o2 = As2DispositionNotificationOptions.valueOf("signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required, sha1");
        assertNotNull(o2.getSignedReceiptMicalg());
        assertEquals(o2.getSignedReceiptMicalg().getTextValue(), "sha1");

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void createFromInvalidString() throws Exception {

        As2DispositionNotificationOptions options = As2DispositionNotificationOptions.valueOf("signed-receipt-protocol=reXXuired, pkcs7-signature");

    }

    @Test
    public void testToString() throws Exception {

        As2DispositionNotificationOptions options = As2DispositionNotificationOptions.valueOf("signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        assertEquals(options.toString(), "signed-receipt-protocol=required,pkcs7-signature; signed-receipt-micalg=required,sha1");

        As2DispositionNotificationOptions opt2 = As2DispositionNotificationOptions.valueOf(options.toString());
        assertNotNull(opt2);
        assertEquals(opt2.getSignedReceiptMicalg().getImportance(), As2DispositionNotificationOptions.Importance.REQUIRED);

    }

}
