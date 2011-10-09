package eu.peppol.inbound.smp;

import eu.peppol.inbound.sml.SmpLookup;
import eu.peppol.inbound.util.TestBase;

import java.net.URLEncoder;

/**
 * User: nigel
 * Date: Oct 8, 2011
 * Time: 12:33:13 PM
 */
//@Test
public class SmpTest extends TestBase {

    public void test01() throws Throwable {
        try {

            String documentScheme = "busdox-docid-qns";
            String documentValue = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0";

            String s = null;

            String dns = "b-6c4fda8cbd88cd212ceedcc3d732087e.iso6523-actorid-upis.sml.peppolcentral.org";

            String receiverScheme = "iso6523-actorid-upis";
            String receiverValue = "0007:SE1122334455";

            String restUrl = "http://" + dns + "/"
                    + URLEncoder.encode(receiverScheme + "::" + receiverValue, "UTF-8")
                    + "/services/"
                    + URLEncoder.encode(documentScheme + "::" + documentValue, "UTF-8");
            try {
                s = SmpLookup.getURLContent(restUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (Throwable t) {
            signal(t);
        }
    }
}
