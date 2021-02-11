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

package network.oxalis.as2.code;

/**
 * @author erlend
 */
public enum DispositionModifierExtension {

    UNSUPPORTED_FORMAT("unsupported format"),

    UNSUPPORTED_MIC_ALGORITHMS("unsupported MIC-algorithms"),

    SENDER_EQUALS_RECEIVER("sender-equals-receiver"),

    DECRYPTION_FAILED("decryption-failed"),

    AUTHENTICATION_FAILED("authentication-failed"),

    INTEGRITY_CHECK_FAILED("integrity-check-failed"),

    PARTICIPANT_NOT_ACCEPTED("participant-not-accepted"),

    DOCUMENT_TYPE_ID_NOT_ACCEPTED("document-modifier-id-not-accepted"),

    PROCESS_ID_NOT_ACCEPTED("process-id-not-accepted"),

    UNEXPECTED_PROCESSING_ERROR("unexpected-processing-error"),

    DUPLICATE_DOCUMENT("duplicate-document");

    private final String value;

    public static DispositionModifierExtension of(String str) {
        for (DispositionModifierExtension extension : values())
            if (extension.value.equals(str))
                return extension;

        throw new IllegalArgumentException(String.format("Unknown disposition modifier extension: %s", str));
    }

    DispositionModifierExtension(String extension) {
        this.value = extension;
    }

    @Override
    public String toString() {
        return value;
    }
}
