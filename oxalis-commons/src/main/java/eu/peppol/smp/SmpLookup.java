/* Created by steinar on 18.05.12 at 13:25 */
package eu.peppol.smp;

import eu.peppol.start.identifier.PeppolDocumentTypeId;
import eu.peppol.start.identifier.ParticipantId;
import eu.peppol.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple SmpLookup service, which does not use JAXB to parse the response from the SMP.
 *
 * This class is experimental and not currently (May 22, 2012) in use.
 *
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class SmpLookup {

    protected static final String SML_PEPPOLCENTRAL_ORG = "sml.peppolcentral.org";
    private ParticipantId participantId;

    Pattern documentTypeIdentifierPattern = Pattern.compile("/services/busdox-docid-qns::(.*)");

    public SmpLookup (ParticipantId participantId){

        this.participantId = participantId;
    }

    URL servicesUrl() throws SmpLookupException {
        String scheme = ParticipantId.getScheme();
        String value = participantId.stringValue();

        try {
            String hostname = "B-" + Util.calculateMD5(value.toLowerCase()) + "." + scheme + "." + SML_PEPPOLCENTRAL_ORG;

            // iso6523-actorid-upis%3A%3A9908:810017902
            String encodedParticipant = URLEncoder.encode(scheme + "::", "UTF-8") + value;

            return new URL("http://" + hostname + "/" + encodedParticipant);
        } catch (Exception e) {
            throw new SmpLookupException(participantId, e);
        }
    }


    public List<URL> getServiceUrlList() throws SmpLookupException {
        URL servicesUrl = servicesUrl();

        InputSource inputSource = Util.getUrlContent(servicesUrl);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(false);    // Makes life with XPath much simpler
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputSource);

            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xPath.compile("//ServiceMetadataReference/@href");

            NodeList nodes = (NodeList)expr.evaluate(document,XPathConstants.NODESET);
            List<URL> result = new ArrayList<URL>();
            for (int i = 0; i < nodes.getLength(); i++) {
                String hrefString = nodes.item(i).getNodeValue();
                result.add(new URL(hrefString));
            }

            return result;
        } catch (Exception e) {
            throw new SmpLookupException(participantId, servicesUrl,e);
        }
    }

    public List<PeppolDocumentTypeId> parseServiceMetadataReferences() throws SmpLookupException {

        List<PeppolDocumentTypeId> documentTypeIdentifiers = new ArrayList<PeppolDocumentTypeId>();

        List<URL> urls = getServiceUrlList();
        for (URL url : urls) {
            // Retrieves the parth of the URL, coming after the hostname
            try {
                String urlPathComponent = URLDecoder.decode(url.getPath(), "UTF-8");
                System.out.println(urlPathComponent);
                Matcher matcher = documentTypeIdentifierPattern.matcher(urlPathComponent);
                if (matcher.find()) {
                    PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeId.valueOf(matcher.group(1));
                    documentTypeIdentifiers.add(documentTypeIdentifier);

                } else
                    throw new IllegalStateException("documentTypeIdentifierPattern did not match ");

            } catch (UnsupportedEncodingException e) {
                throw new SmpLookupException(participantId, e);
            }
        }

        return documentTypeIdentifiers;
    }
}
