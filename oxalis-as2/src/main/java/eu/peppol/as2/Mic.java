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

package eu.peppol.as2;

/**
 * Value object holding the Message Integrity Control (MIC) of an AS2 message.
 *
 * @author steinar
 */
public class Mic {

    private final String digestAsString;
    private final String algorithmName;

    public Mic(String digestAsString, String algorithmName) {
        this.digestAsString = digestAsString;
        this.algorithmName = algorithmName;
    }

    public static Mic valueOf(String receivedContentMic) {
        String s[] = receivedContentMic.split(",");
        if (s.length != 2) {
            throw new IllegalArgumentException("Invalid mic: '" + receivedContentMic + "'. Required syntax: encoded-message-digest \",\" (sha1|md5)");
        }
        return new Mic(s[0].trim(), s[1].trim());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(digestAsString).append(", ").append(algorithmName);
        return sb.toString();
    }

}
