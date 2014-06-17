/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.as2;

/**
 * Value object holding the MIC of an AS2 message.
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
