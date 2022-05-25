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

import com.google.common.collect.ImmutableMap;
import network.oxalis.api.lang.VerifierException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author erlend
 */
public class Disposition {

    private static final Pattern PATTERN = Pattern.compile("^(.*?); ([a-z]+)/([a-z]+): (.*)|(.*?); ([a-z]+)$");

    private static final String SENT_AUTOMATICALLY = "automatic-action/MDN-sent-automatically";

    public static final Disposition PROCESSED = new Disposition(
            DispositionType.PROCESSED, null, null);

    public static final Disposition UNSUPPORTED_FORMAT = new Disposition(
            DispositionType.FAILED, DispositionModifier.FAILURE,
            DispositionModifierExtension.UNEXPECTED_PROCESSING_ERROR);

    public static final Disposition UNSUPPORTED_MIC_ALGORITHMS = new Disposition(
            DispositionType.FAILED, DispositionModifier.FAILURE,
            DispositionModifierExtension.UNSUPPORTED_MIC_ALGORITHMS);

    public static final Disposition SENDER_EQUALS_RECEIVER = new Disposition(
            DispositionType.FAILED, DispositionModifier.FAILURE,
            DispositionModifierExtension.SENDER_EQUALS_RECEIVER);

    public static final Disposition DECRYPTION_FAILED = new Disposition(
            DispositionType.FAILED, DispositionModifier.ERROR,
            DispositionModifierExtension.DECRYPTION_FAILED);

    public static final Disposition AUTHENTICATION_FAILED = new Disposition(
            DispositionType.FAILED, DispositionModifier.ERROR,
            DispositionModifierExtension.AUTHENTICATION_FAILED);

    public static final Disposition INTEGRITY_CHECK_FAILED = new Disposition(
            DispositionType.FAILED, DispositionModifier.ERROR,
            DispositionModifierExtension.INTEGRITY_CHECK_FAILED);

    public static final Disposition PARTICIPANT_NOT_ACCEPTED = new Disposition(
            DispositionType.FAILED, DispositionModifier.ERROR,
            DispositionModifierExtension.PARTICIPANT_NOT_ACCEPTED);

    public static final Disposition DOCUMENT_TYPE_ID_NOT_ACCEPTED = new Disposition(
            DispositionType.FAILED, DispositionModifier.ERROR,
            DispositionModifierExtension.DOCUMENT_TYPE_ID_NOT_ACCEPTED);

    public static final Disposition PROCESS_ID_NOT_ACCEPTED = new Disposition(
            DispositionType.FAILED, DispositionModifier.ERROR,
            DispositionModifierExtension.PROCESS_ID_NOT_ACCEPTED);

    public static final Disposition UNEXPECTED_PROCESSING_ERROR = new Disposition(
            DispositionType.FAILED, DispositionModifier.ERROR,
            DispositionModifierExtension.UNEXPECTED_PROCESSING_ERROR);

    public static final Disposition DUPLICATE_DOCUMENT = new Disposition(
            DispositionType.PROCESSED, DispositionModifier.WARNING,
            DispositionModifierExtension.DUPLICATE_DOCUMENT);

    private static Map<VerifierException.Reason, Disposition> verifierMap =
            ImmutableMap.<VerifierException.Reason, Disposition>builder()
                    .put(VerifierException.Reason.DOCUMENT_TYPE, DOCUMENT_TYPE_ID_NOT_ACCEPTED)
                    .put(VerifierException.Reason.PROCESS, PROCESS_ID_NOT_ACCEPTED)
                    .put(VerifierException.Reason.PARTICIPANT, PARTICIPANT_NOT_ACCEPTED)
                    .build();

    private DispositionType type;

    private DispositionModifier modifier;

    private DispositionModifierExtension extension;

    public static Disposition parse(String str) {
        // Matcher matcher = PATTERN.matcher(str);
        String cleaned = str.replaceAll("[ \r\n\t]+", " ");

        Matcher matcher = PATTERN.matcher(cleaned);

        if (matcher.matches()) {
            if (matcher.group(1) == null)
                return new Disposition(
                        DispositionType.of(matcher.group(6)),
                        null, null
                );
            else
                return new Disposition(
                        DispositionType.of(matcher.group(2)),
                        DispositionModifier.of(matcher.group(3)),
                        DispositionModifierExtension.of(matcher.group(4))
                );
        }

        throw new IllegalStateException(String.format("Unable to parseOld disposition '%s'.", str));
    }

    public static Disposition fromVerifierException(VerifierException e) {
        return verifierMap.get(e.getReason());
    }

    private Disposition(DispositionType type, DispositionModifier modifier, DispositionModifierExtension extension) {
        this.type = type;
        this.modifier = modifier;
        this.extension = extension;
    }

    public DispositionType getType() {
        return type;
    }

    public DispositionModifier getModifier() {
        return modifier;
    }

    public DispositionModifierExtension getExtension() {
        return extension;
    }

    @Override
    public String toString() {
        if (modifier == null)
            return String.format("%s; %s", SENT_AUTOMATICALLY, type);
        else
            return String.format("%s; %s/%s: %s", SENT_AUTOMATICALLY, type, modifier, extension);
    }
}
