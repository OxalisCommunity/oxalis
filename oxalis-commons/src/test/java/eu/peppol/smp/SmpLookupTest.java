/* Created by steinar on 18.05.12 at 13:41 */
package eu.peppol.smp;

import eu.peppol.start.identifier.PeppolDocumentTypeId;
import eu.peppol.start.identifier.ParticipantId;
import org.testng.annotations.Test;

import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class SmpLookupTest {

    @Test
    public void lookupServicesUrl() throws SmpLookupException {
        SmpLookup smpLookup = new SmpLookup(new ParticipantId("9908:810017902"));
        URL url = smpLookup.servicesUrl();
        assertEquals(url.toExternalForm(), "http://B-ddc207601e442e1b751e5655d39371cd.iso6523-actorid-upis."+ SmpLookup.SML_PEPPOLCENTRAL_ORG+"/iso6523-actorid-upis%3A%3A9908:810017902");
    }

    @Test
    public void performLookup() throws SmpLookupException {
        SmpLookup smpLookup = new SmpLookup(new ParticipantId("9908:810017902"));
        List<URL> result = smpLookup.getServiceUrlList();

        for (URL url : result) {

            String s = URLDecoder.decode(url.getPath());
            // All URL encoded characters shall have been translated, so we don't expected to see any "%" characters
            assertTrue(s.indexOf("%") < 0);
        }
    }

    @Test
    public void parseServiceMetadataReferences() throws SmpLookupException {
        SmpLookup smpLookup = new SmpLookup(new ParticipantId("9908:810017902"));

        List<PeppolDocumentTypeId> result = smpLookup.parseServiceMetadataReferences();
        for (PeppolDocumentTypeId documentTypeIdentifier : result) {
            System.out.println(documentTypeIdentifier);
        }
    }

    @Test
    public void parse() {
        Pattern p = Pattern.compile("(.*):#([^#]*)(?:#(.*))?");
        Matcher m = p.matcher("/iso6523-actorid-upis::9908:810017902/services/busdox-docid-qns::urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.cenbii.eu:profile:biixx:ver1.0#urn:www.difi.no:ehf:kreditnota:ver1::2.0");
        if (m.find()) {
            System.out.println("Match");

            for (int i = 1; i <= m.groupCount(); i++){
                System.out.println(i + ": " + m.group(i));
            }
        } else {
            System.out.println("No match!");
        }
    }
}
