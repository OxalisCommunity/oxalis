/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package eu.peppol.as2.code;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.peppol.as2.code.DispositionModifier.*;
import static eu.peppol.as2.code.DispositionModifierExtension.*;
import static eu.peppol.as2.code.DispositionType.FAILED;

/**
 * @author erlend
 */
public class Disposition {

    private static final Pattern PATTERN = Pattern.compile("^(.*?); ([a-z]+)/([a-z]+): (.*)|(.*?); ([a-z]+)$");

    private static final String SENT_AUTOMATICALLY = "automatic-action/MDN-sent-automatically";

    public static final Disposition PROCESSED =
            new Disposition(DispositionType.PROCESSED, null, null);

    public static final Disposition FAILURE_UNSUPPORTED_FORMAT =
            new Disposition(FAILED, FAILURE, UNEXPECTED_PROCESSING_ERROR);

    public static final Disposition FAILURE_UNSUPPORTED_MIC_ALGORITHMS =
            new Disposition(FAILED, FAILURE, UNSUPPORTED_MIC_ALGORITHMS);

    public static final Disposition FAILURE_SENDER_EQUALS_RECEIVER =
            new Disposition(FAILED, FAILURE, SENDER_EQUALS_RECEIVER);

    public static final Disposition ERROR_DECRYPTION_FAILED =
            new Disposition(FAILED, ERROR, DECRYPTION_FAILED);

    public static final Disposition ERROR_AUTHENTICATION_FAILED =
            new Disposition(FAILED, ERROR, AUTHENTICATION_FAILED);

    public static final Disposition ERROR_INTEGRITY_CHECK_FAILED =
            new Disposition(FAILED, ERROR, INTEGRITY_CHECK_FAILED);

    public static final Disposition ERROR_PARTICIPANT_NOT_ACCEPTED =
            new Disposition(FAILED, ERROR, PARTICIPANT_NOT_ACCEPTED);

    public static final Disposition ERROR_DOCUMENT_TYPE_ID_NOT_ACCEPTED =
            new Disposition(FAILED, ERROR, DOCUMENT_TYPE_ID_NOT_ACCEPTED);

    public static final Disposition ERROR_PROCESS_ID_NOT_ACCEPTED =
            new Disposition(FAILED, ERROR, PROCESS_ID_NOT_ACCEPTED);

    public static final Disposition ERROR_UNEXPECTED_PROCESSING_ERROR =
            new Disposition(FAILED, ERROR, UNEXPECTED_PROCESSING_ERROR);

    public static final Disposition WARNING_DUPLICATE_DOCUMENT =
            new Disposition(DispositionType.PROCESSED, WARNING, DUPLICATE_DOCUMENT);


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

        throw new IllegalStateException(String.format("Unable to parse disposition '%s'.", str));
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
