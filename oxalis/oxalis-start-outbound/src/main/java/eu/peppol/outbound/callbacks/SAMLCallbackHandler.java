/*
 * Version: MPL 1.1/EUPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Copyright The PEPPOL project (http://www.peppol.eu)
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL
 * (the "Licence"); You may not use this work except in compliance
 * with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * If you wish to allow use of your version of this file only
 * under the terms of the EUPL License and not to allow others to use
 * your version of this file under the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and
 * other provisions required by the EUPL License. If you do not delete
 * the provisions above, a recipient may use your version of this file
 * under either the MPL or the EUPL License.
 */
/*
 * Version: MPL 1.1/EUPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Copyright The PEPPOL project (http://www.peppol.eu)
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL
 * (the "Licence"); You may not use this work except in compliance
 * with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * If you wish to allow use of your version of this file only
 * under the terms of the EUPL License and not to allow others to use
 * your version of this file under the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and
 * other provisions required by the EUPL License. If you do not delete
 * the provisions above, a recipient may use your version of this file
 * under either the MPL or the EUPL License.
 */
package eu.peppol.outbound.callbacks;

import com.sun.xml.wss.impl.callback.SAMLCallback;
import com.sun.xml.wss.impl.dsig.WSSPolicyConsumerImpl;
import com.sun.xml.wss.saml.*;
import eu.peppol.outbound.util.Log;
import eu.peppol.start.identifier.Configuration;
import eu.peppol.start.identifier.KeystoreManager;
import org.w3c.dom.*;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.soap.MessageFactory;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * The SAMLCallbackHandler is the CallbackHandler implementation used to deal with SAML authentication.
 *
 * @author Alexander Aguirre Julcapoma(alex@alfa1lab.com)
 *         Jose Gorvenia Narvaez(jose@alfa1lab.com)
 */
public class SAMLCallbackHandler implements CallbackHandler {

    private final String SENDER_NAME_ID_SYNTAX = "http://busdox.org/profiles/serviceMetadata/1.0/UniversalBusinessIdentifier/1.0/";
    private final String ACCESSPOINT_NAME_ID_SYNTAX = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
    private final String CONFIRMATION_METHOD = "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches";
    private final String AUTHENTICATION_CONTEXT_TYPE = "urn:oasis:names:tc:SAML:2.0:ac:classes:X509";
    private final String ATTRIBUTE_NAME = "urn:eu:busdox:attribute:assurance-level";
    private final String ATTRIBUTE_NAMESPACE = "urn:oasis:names:tc:SAML:2.0:attrname-format:basic";

    /**
     * Retrieve or display the information requested in the provided Callbacks.
     *
     * @param callbacks an array of Callback objects provided by an underlying
     *                  security service which contains the information
     *                  requested to be retrieved or displayed.
     * @throws IOException                  if an input or output error occurs.
     * @throws UnsupportedCallbackException if the implementation of this method
     *                                      does not support one or more of the Callbacks specified in
     *                                      the callbacks parameter.
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        Log.debug("Requested SAML callback handling");

        for (Callback callback : callbacks) {

            if (callback instanceof SAMLCallback) {
                SAMLCallback samlCallback = (SAMLCallback) callback;

                try {
                    if (samlCallback.getConfirmationMethod().equals(SAMLCallback.SV_ASSERTION_TYPE)) {
                        Element element = createSenderVouchesSAMLAssertion(samlCallback);
                        samlCallback.setAssertionElement(element);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error while handling SAML callbacks", e);
                }

            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    /**
     * Gets an Element representing SAML Assertion.
     *
     * @param samlCallback the SAMLCallback.
     * @return an Element.
     * @throws Exception thrown if there is a SOAP problem.
     */
    private Element createSenderVouchesSAMLAssertion(SAMLCallback samlCallback) throws Exception {

        Log.debug("Creating and setting the SAML Sender Vouches Assertion");

        KeystoreManager keystoreManager = new KeystoreManager();

        Configuration configuration = Configuration.getInstance();
        String senderId = configuration.getPeppolSenderId();
        String accesspointName = configuration.getPeppolServiceName();

        String assertionID = "SamlID" + String.valueOf(System.currentTimeMillis());
        samlCallback.setAssertionId(assertionID);

        GregorianCalendar oneHourAgo = getNowOffsetByHours(-1);
        GregorianCalendar now = getNowOffsetByHours(0);
        GregorianCalendar inOneHour = getNowOffsetByHours(1);

        SAMLAssertionFactory assertionFactory = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML2_0);

        NameID senderNameID = assertionFactory.createNameID(senderId, null, SENDER_NAME_ID_SYNTAX);
        NameID accessPointNameID = assertionFactory.createNameID(accesspointName, null, ACCESSPOINT_NAME_ID_SYNTAX);
        SubjectConfirmation subjectConfirmation = assertionFactory.createSubjectConfirmation(null, CONFIRMATION_METHOD);
        Subject subject = assertionFactory.createSubject(senderNameID, subjectConfirmation);
        AuthnContext authnContext = assertionFactory.createAuthnContext(AUTHENTICATION_CONTEXT_TYPE, null);
        AuthnStatement authnStatement = assertionFactory.createAuthnStatement(now, null, authnContext, null, null);

        List<Object> statements = new LinkedList<Object>();
        statements.add(authnStatement);
        // FIXME: eu hard coding of security assurance level
        statements.add(getAssuranceLevelStatement("2", assertionFactory));
        Conditions conditions = assertionFactory.createConditions(oneHourAgo, inOneHour, null, null, null, null);

        Assertion assertion = assertionFactory.createAssertion(
                assertionID,
                accessPointNameID,
                now,
                conditions,
                null,
                subject,
                statements);

        return sign(assertion, keystoreManager.getOurCertificate(), keystoreManager.getOurPrivateKey());
    }

    private GregorianCalendar getNowOffsetByHours(int hours) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        long now = gregorianCalendar.getTimeInMillis();
        long then = now + (60L * 60L * 1000L * hours);
        gregorianCalendar.setTimeInMillis(then);
        return gregorianCalendar;
    }

    /**
     * Gets the Level Statement of Assurance. Assuarnace levels are defined in
     * http://csrc.nist.gov/publications/nistpubs/800-63/SP800-63V1_0_2.pdf
     *
     * @param assuranceLevel    the assurance level.
     * @param samlAssertFactory the SAMLAssertionFactory.
     * @return an AttributeStatement.
     * @throws SAMLException Throws a SAMLException.
     */
    private AttributeStatement getAssuranceLevelStatement(String assuranceLevel, SAMLAssertionFactory samlAssertFactory)
            throws SAMLException {

        List<String> values = new ArrayList<String>();
        values.add(assuranceLevel);

        Attribute attribute = samlAssertFactory.createAttribute(ATTRIBUTE_NAME, ATTRIBUTE_NAMESPACE, values);
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(attribute);

        return samlAssertFactory.createAttributeStatement(attributes);
    }

    /**
     * Converts a SAML Assertion to an org.w3c.dom.Element object and signs it
     * with the X509Certificate and PrivateKey using the SHA1 DigestMethod and
     * the RSA_SHA1 SignatureMethod.
     *
     * @param assertion   SAML Assertion to be signed.
     * @param certificate the X509Certificate.
     * @param privateKey  the certificate's private key.
     * @return a signed org.w3c.dom.Element holding the SAML Assertion.
     * @throws SAMLException Throws a SAMLException.
     */
    public final Element sign(Assertion assertion, X509Certificate certificate, PrivateKey privateKey) throws SAMLException {

        try {

            XMLSignatureFactory signatureFactory = WSSPolicyConsumerImpl.getInstance().getSignatureFactory();
            DigestMethod digestMethod = signatureFactory.newDigestMethod(DigestMethod.SHA1, null);
            return sign(assertion, digestMethod, SignatureMethod.RSA_SHA1, certificate, privateKey);

        } catch (Exception ex) {
            throw new SAMLException(ex);
        }
    }

    /**
     * Converts a SAML Assertion to an org.w3c.dom.Element object and signs it with the X509Certificate and private key
     * using the given DigestMethod and the specified SignatureMethod.
     *
     * @param assertion       SAML Assertion to be signed.
     * @param digestMethod    the digest method.
     * @param signatureMethod the signature method.
     * @param certificate     the X509Certificate.
     * @param privateKey      the certificate's private key.
     * @return a signed org.w3c.dom.Element holding the SAML Assertion.
     * @throws SAMLException Throws a SAMLException.
     */
    public final Element sign(Assertion assertion,
                              DigestMethod digestMethod,
                              String signatureMethod,
                              X509Certificate certificate,
                              PrivateKey privateKey)
            throws SAMLException {

        try {
            XMLSignatureFactory signatureFactory = WSSPolicyConsumerImpl.getInstance().getSignatureFactory();

            List<Transform> transformList = new ArrayList<Transform>();
            transformList.add(signatureFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
            transformList.add(signatureFactory.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null));

            Reference reference = signatureFactory.newReference("#" + assertion.getID(), digestMethod, transformList, null, null);

            CanonicalizationMethod canonicalizationMethod =
                    signatureFactory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null);

            SignedInfo signedInfo = signatureFactory.newSignedInfo(
                    canonicalizationMethod,
                    signatureFactory.newSignatureMethod(signatureMethod, null),
                    Collections.singletonList(reference));

            Document document = MessageFactory.newInstance().createMessage().getSOAPPart();
            KeyInfoFactory keyInfoFactory = signatureFactory.getKeyInfoFactory();

            X509Data x509Data = keyInfoFactory.newX509Data(Collections.singletonList(certificate));
            KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(x509Data));

            Element assertionElement = assertion.toElement(document);
            DOMSignContext domSignContext = new DOMSignContext(privateKey, assertionElement);

            XMLSignature xmlSignature = signatureFactory.newXMLSignature(signedInfo, keyInfo);
            domSignContext.putNamespacePrefix("http://www.w3.org/2000/09/xmldsig#", "ds");
            xmlSignature.sign(domSignContext);
            placeSignatureAfterIssuer(assertionElement);

            return assertionElement;

        } catch (Exception e) {
            throw new SAMLException(e);
        }
    }

    /**
     * Places a Signature.
     *
     * @param assertionElement an Element containing an Assertion.
     * @throws DOMException Throws a DOMException.
     */
    private void placeSignatureAfterIssuer(final Element assertionElement)
            throws DOMException {

        NodeList nodes = assertionElement.getChildNodes();
        List<Node> movingNodes = new ArrayList<Node>();

        for (int i = 1; i < nodes.getLength() - 1; i++) {
            movingNodes.add(nodes.item(i));
        }

        for (Node node : movingNodes) {
            assertionElement.removeChild(node);
        }

        for (Node node : movingNodes) {
            assertionElement.appendChild(node);
        }
    }
}
