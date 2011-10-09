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
package eu.peppol.outbound.saml;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.callback.SAMLCallback;
import com.sun.xml.wss.impl.dsig.WSSPolicyConsumerImpl;
import com.sun.xml.wss.saml.*;
import eu.peppol.outbound.util.Log;
import eu.peppol.start.util.Configuration;
import org.w3c.dom.*;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * The SAMLCallbackHandler is the CallbackHandler implementation used for
 * deal with SAML authentication.
 *
 * @author Alexander Aguirre Julcapoma(alex@alfa1lab.com)
 *         Jose Gorvenia Narvaez(jose@alfa1lab.com)
 */
public class SAMLCallbackHandler implements CallbackHandler {

    /**
     * Represents the Sender ID for an operation.
     */
    public static final String SENDER_ID = "peppol.senderid";

    /**
     * Represents the Trustore URL.
     */
    public static final String KEYSTORE_FILE = "keystore";

    /**
     * Represents the Trustore Password.
     */
    public static final String KEYSTORE_PASSWORD = "keystore.password";

    /**
     * Represents a SAML Issuer.
     */
    public static final String SAML_TOKEN_ISSUER_NAME = "peppol.servicename";

    /**
     * Assertion ID prefix.
     */
    private final String SAML_ID_PREFIX = "SamlID";

    /**
     * Sender ID syntax.
     */
    private final String SENDER_NAME_ID_SYNTAX = "http://busdox.org/profiles/serviceMetadata/1.0/UniversalBusinessIdentifier/1.0/";

    /**
     * Accesspoint ID syntax.
     */
    private final String ACCESSPOINT_NAME_ID_SYNTAX = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";

    /**
     * Authentication type.
     */
    private final String CONFIRMATION_METHOD = "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches";

    /**
     * Authentication context schema type.
     */
    private final String AUTHENTICATION_CONTEXT_TYPE = "urn:oasis:names:tc:SAML:2.0:ac:classes:X509";

    /**
     * Attribute Assurance Level.
     */
    private final String ATTRIBUTE_NAME = "urn:eu:busdox:attribute:assurance-level";

    /**
     * Attribute Namespace.
     */
    private final String ATTRIBUTE_NAMESPACE = "urn:oasis:names:tc:SAML:2.0:attrname-format:basic";

    /**
     * Exception for operations in this class.
     */
    private UnsupportedCallbackException unsupported = new UnsupportedCallbackException(null,
            "Unsupported Callback Type Encountered");

    /**
     * Counter initialized by default.
     */
    private int count = 0;

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
    public void handle(final Callback[] callbacks) throws IOException,
            UnsupportedCallbackException {

        for (Callback callback : callbacks) {

            if (callback instanceof SAMLCallback) {

                try {
                    SAMLCallback samlCallback = (SAMLCallback) callback;
                    if (samlCallback.getConfirmationMethod().equals(SAMLCallback.SV_ASSERTION_TYPE) && count == 0) {
                        samlCallback.setAssertionElement(createSenderVouchesSAMLAssertion(samlCallback));
                    }
                } catch (Exception ex) {
                    Log.error("Error while handling callbacks", ex);
                }

            } else {
                throw unsupported;
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
    private Element createSenderVouchesSAMLAssertion(final SAMLCallback samlCallback) throws Exception {

        Log.info("Creating and setting the SAML Sender Vouches Assertion");

        Assertion assertion = null;

        //SAMLConfiguration configuration = SAMLConfiguration.getInstance();
        Configuration configuration = Configuration.getInstance();
        String senderId = configuration.getProperty(SENDER_ID);
        String accesspointName = configuration.getProperty(SAML_TOKEN_ISSUER_NAME);
        String storeFilename = configuration.getProperty(KEYSTORE_FILE);
        String storePassword = configuration.getProperty(KEYSTORE_PASSWORD);

        String assertionID = SAML_ID_PREFIX + String.valueOf(System.currentTimeMillis()); //id must start with letters (WIF .NET)
        samlCallback.setAssertionId(assertionID); //neccesary to support <sp:ProtectTokens>

        GregorianCalendar c = new GregorianCalendar();
        long beforeTime = c.getTimeInMillis();

        // Roll the time by one hour
        long offsetHours = 60 * 60 * 1000;
        c.setTimeInMillis(beforeTime - offsetHours);
        GregorianCalendar before = (GregorianCalendar) c.clone();

        c = new GregorianCalendar();
        long afterTime = c.getTimeInMillis();
        c.setTimeInMillis(afterTime + offsetHours);
        GregorianCalendar after = (GregorianCalendar) c.clone();

        GregorianCalendar issueInstant = new GregorianCalendar();

        // Statements
        List statements = new LinkedList();
        SAMLAssertionFactory factory = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML2_0);

        // Setup SenderNameID
        NameID senderNameID = factory.createNameID(senderId, null, SENDER_NAME_ID_SYNTAX);

        // Setup AccessPointID
        NameID accessPointNameID = factory.createNameID(accesspointName, null, ACCESSPOINT_NAME_ID_SYNTAX);

        SubjectConfirmation scf = factory.createSubjectConfirmation(null, CONFIRMATION_METHOD);
        Subject subj = factory.createSubject(senderNameID, scf);

        AuthnContext ctx = factory.createAuthnContext(AUTHENTICATION_CONTEXT_TYPE, null);

        AuthnStatement statement = factory.createAuthnStatement(issueInstant, null, ctx, null, null);

        statements.add(statement);
        String assuranceLevel = "3";
        statements.add(getAussuranceLevelStatement(assuranceLevel, factory));
        Conditions conditions = factory.createConditions(before, after, null, null, null, null);
        assertion = factory.createAssertion(assertionID, accessPointNameID, issueInstant, conditions, null, subj, statements);
        X509Certificate accessPointCertificate = getCertificate(storeFilename, storePassword);
        Element signedAssertion = sign(assertion, accessPointCertificate, getPrivateKey(storeFilename, storePassword));

        return signedAssertion;
    }

    /**
     * Gets the Level Statement of Assurance.
     *
     * @param assuranceLevel    the assurance level.
     * @param samlAssertFactory the SAMLAssertionFactory.
     * @return an AttributeStatement.
     * @throws SAMLException Throws a SAMLException.
     */
    private AttributeStatement getAussuranceLevelStatement(final String assuranceLevel,
                                                           final SAMLAssertionFactory samlAssertFactory) throws SAMLException {

        List<Attribute> attrs = new ArrayList<Attribute>();
        List<String> values = new ArrayList<String>();
        values.add(assuranceLevel);

        Attribute attr = samlAssertFactory.createAttribute(ATTRIBUTE_NAME,
                ATTRIBUTE_NAMESPACE, values);
        attrs.add(attr);

        AttributeStatement statement = samlAssertFactory.createAttributeStatement(attrs);

        return statement;
    }

    /**
     * Gets a X509Certificate object given the JKS URL location and
     * its password.
     *
     * @param storeUrl      URL where the X.509 certificate is stored.
     * @param storePassword certificate password.
     * @return the X509Certificate instance.
     * @throws NoSuchAlgorithmException Throws a NoSuchAlgorithmException.
     * @throws CertificateException     Throws a CertificateException.
     * @throws NoSuchProviderException  Throws a NoSuchProviderException.
     * @throws IOException              Throws an IOException.
     * @throws KeyStoreException        Throws a KeyStoreException.
     */
    public static X509Certificate getCertificate(final String storeUrl,
                                                 final String storePassword)
            throws NoSuchAlgorithmException, CertificateException,
            NoSuchProviderException, IOException, KeyStoreException {

        KeyStore keystore = getKeyStore(storeUrl, storePassword);
        String alias = keystore.aliases().nextElement();

        return (X509Certificate) keystore.getCertificate(alias);
    }

    /**
     * Gets a PrivateKey instance given a JKS URL store location and a password.
     *
     * @param storeUrl      the JKS URL location.
     * @param storePassword the password.
     * @return the PrivateKey.
     * @throws KeyStoreException         Throws a KeyStoreException.
     * @throws NoSuchAlgorithmException  Throws a NoSuchAlgorithmException.
     * @throws IOException               Throws an IOException.
     * @throws CertificateException      Throws a CertificateException.
     * @throws NoSuchProviderException   Throws a NoSuchProviderException.
     * @throws UnrecoverableKeyException Throws an UnrecoverableKeyException.
     */
    public static PrivateKey getPrivateKey(final String storeUrl,
                                           final String storePassword)
            throws KeyStoreException, NoSuchAlgorithmException, IOException,
            CertificateException, NoSuchProviderException,
            UnrecoverableKeyException {

        PrivateKey privateKey = null;
        KeyStore keystore = getKeyStore(storeUrl, storePassword);
        String alias = keystore.aliases().nextElement();
        Key key = keystore.getKey(alias, storePassword.toCharArray());

        if (key instanceof PrivateKey) {
            Certificate cert = keystore.getCertificate(alias);
            PublicKey publicKey = cert.getPublicKey();
            KeyPair keyPair = new KeyPair(publicKey, (PrivateKey) key);
            privateKey = keyPair.getPrivate();
        }

        return privateKey;
    }

    /**
     * Gets a Keystore by loading its content by using the given certificate
     * url and password. The KeyStore must be of JKS type and from SUN provider.
     *
     * @param storeUrl      url where the certificate is stored.
     * @param storePassword certificate password.
     * @return a KeyStore.
     * @throws KeyStoreException        Throws a KeyStoreException.
     * @throws IOException              Throws an IOException.
     * @throws NoSuchAlgorithmException Throws a NoSuchAlgorithmException.
     * @throws CertificateException     Throws a CertificateException.
     * @throws NoSuchProviderException  Throws a NoSuchProviderException.
     */
    public static KeyStore getKeyStore(final String storeUrl,
                                       final String storePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException, NoSuchProviderException {

        return loadStore(storeUrl, storePassword);
    }

    /**
     * Loads a Keystore by loading its content by using the given certificate
     * url and password. The KeyStore must be of JKS type and from SUN provider.
     *
     * @param storeUrl      URL where the certificate is stored.
     * @param storePassword certificate password.
     * @return the KeyStore instance.
     * @throws KeyStoreException        Throws a KeyStoreException.
     * @throws IOException              Throws an IOException.
     * @throws NoSuchAlgorithmException Throws a NoSuchAlgorithmException.
     * @throws CertificateException     Throws a CertificateException.
     * @throws NoSuchProviderException  Throws a NoSuchProviderException.
     */
    private static KeyStore loadStore(final String storeUrl,
                                      final String storePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException, NoSuchProviderException {

        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream(new File(storeUrl));
        ks.load(fis, storePassword.toCharArray());

        fis.close();

        return ks;
    }

    /**
     * Converts a SAML Assertion to an org.w3c.dom.Element object and signs it
     * with the X509Certificate and PrivateKey using the SHA1 DigestMethod and
     * the RSA_SHA1 SignatureMethod.
     *
     * @param assertion SAML Assertion to be signed.
     * @param cert      the X509Certificate.
     * @param privKey   the certificate's private key.
     * @return a signed org.w3c.dom.Element holding the SAML Assertion.
     * @throws SAMLException Throws a SAMLException.
     */
    public final Element sign(final Assertion assertion,
                              final X509Certificate cert, final PrivateKey privKey)
            throws SAMLException {

        try {
            XMLSignatureFactory fac = WSSPolicyConsumerImpl.getInstance().getSignatureFactory();

            return sign(assertion, fac.newDigestMethod(DigestMethod.SHA1, null),
                    SignatureMethod.RSA_SHA1, cert, privKey);
        } catch (Exception ex) {
            throw new SAMLException(ex);
        }
    }

    /**
     * Converts a SAML Assertion to an org.w3c.dom.Element object and signs it
     * with the X509Certificate and PrivateKey using the given DigestMethod and
     * the specified SignatureMethod.
     *
     * @param assertion       SAML Assertion to be signed.
     * @param digestMethod    the digest method.
     * @param signatureMethod the signature method.
     * @param cert            the X509Certificate.
     * @param privKey         the certificate's private key.
     * @return a signed org.w3c.dom.Element holding the SAML Assertion.
     * @throws SAMLException Throws a SAMLException.
     */
    public final Element sign(final Assertion assertion,
                              final DigestMethod digestMethod, final String signatureMethod,
                              final X509Certificate cert, final PrivateKey privKey)
            throws SAMLException {

        try {
            XMLSignatureFactory fac = WSSPolicyConsumerImpl.getInstance().getSignatureFactory();
            ArrayList transformList = new ArrayList();

            Transform tr1 = fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
            Transform tr2 = fac.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null);
            transformList.add(tr1);
            transformList.add(tr2);

            String uri = "#" + assertion.getID();
            Reference ref = fac.newReference(uri, digestMethod,
                    transformList, null, null);

            // Create the SignedInfo
            SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod(signatureMethod, null),
                    Collections.singletonList(ref));

            /* Document to be signed */
            Document doc = MessageFactory.newInstance().createMessage().getSOAPPart();
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            javax.xml.crypto.dsig.keyinfo.KeyInfo ki = null;

            X509Data x509Data = kif.newX509Data(Collections.singletonList(cert));
            ki = kif.newKeyInfo(Collections.singletonList(x509Data));

            Element assertionElement = assertion.toElement(doc);
            DOMSignContext dsc = new DOMSignContext(privKey, assertionElement);
            HashMap map = new HashMap();
            map.put(assertion.getID(), assertionElement);

            XMLSignature signature = fac.newXMLSignature(si, ki);
            dsc.putNamespacePrefix("http://www.w3.org/2000/09/xmldsig#", "ds");

            signature.sign(dsc);
            placeSignatureAfterIssuer(assertionElement);

            return assertionElement;
        } catch (XWSSecurityException ex) {
            throw new SAMLException(ex);
        } catch (MarshalException ex) {
            throw new SAMLException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new SAMLException(ex);
        } catch (SOAPException ex) {
            throw new SAMLException(ex);
        } catch (XMLSignatureException ex) {
            throw new SAMLException(ex);
        } catch (InvalidAlgorithmParameterException ex) {
            throw new SAMLException(ex);
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
