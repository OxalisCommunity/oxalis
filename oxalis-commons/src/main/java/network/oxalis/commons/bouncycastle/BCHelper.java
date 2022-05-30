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

package network.oxalis.commons.bouncycastle;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

/**
 * Collection of functionality related to BouncyCastle.
 *
 * @author erlend
 * @since 4.0.0
 */
public class BCHelper {

    static {
        registerProvider();
    }

    /**
     * Registers BouncyCastle as provider if not already registered.
     */
    public static void registerProvider() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Creates a MessageDigest object using the BouncyCastle provider. Exception {@link NoSuchProviderException} is
     * disguised as {@link NoSuchAlgorithmException}.
     *
     * @param algorithm Algorithm to be use to create the MessageDigest object.
     * @return MessageDigest object ready for use.
     * @throws NoSuchAlgorithmException Thrown in cases when unknown algorithms are requestes.
     */
    public static MessageDigest getMessageDigest(String algorithm) throws NoSuchAlgorithmException {
        try {
            return MessageDigest.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);
        } catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(e.getMessage(), e);
        }
    }
}
