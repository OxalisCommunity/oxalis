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

import network.oxalis.as2.util.SMimeDigestMethod;
import network.oxalis.vefa.peppol.common.model.Digest;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

/**
 * Value object holding the Message Integrity Control (MIC) of an AS2 message.
 *
 * @author steinar
 */
public class Mic {

    private final String digestAsString;

    private final SMimeDigestMethod algorithm;

    public Mic(Digest digest) {
        this(Base64.getEncoder().encodeToString(digest.getValue()),
                SMimeDigestMethod.findByDigestMethod(digest.getMethod()));
    }

    public Mic(String digestAsString, SMimeDigestMethod algorithm) {
        this.digestAsString = digestAsString;
        this.algorithm = algorithm;
    }

    public static Mic valueOf(String receivedContentMic) throws NoSuchAlgorithmException {
        String s[] = receivedContentMic.split(",");
        if (s.length != 2) {
            throw new IllegalArgumentException("Invalid mic: '" + receivedContentMic + "'. Required syntax: encoded-message-digest \",\" (sha1|md5)");
        }
        return new Mic(s[0].trim(), SMimeDigestMethod.findByIdentifier(s[1].trim()));
    }

    @Override
    public String toString() {
        return String.format("%s, %s", digestAsString, algorithm.getIdentifier());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mic mic = (Mic) o;
        return Objects.equals(digestAsString, mic.digestAsString) &&
                algorithm == mic.algorithm;
    }

    @Override
    public int hashCode() {
        return Objects.hash(digestAsString, algorithm);
    }
}
