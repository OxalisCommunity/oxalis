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

package network.oxalis.sniffer.identifier;

import network.oxalis.sniffer.lang.InvalidPeppolParticipantException;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
import network.oxalis.vefa.peppol.icd.api.Icd;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Steinar Overbeck Cook
 * @author Thore Johnsen
 * @see SchemeId
 */
public class ParticipantId implements Serializable {

    private static final Pattern ISO6523_PATTERN = Pattern.compile("^(\\d{4}):([^\\s]+)$");

    //max length for international organisation number
    private static final int INTERNATION_ORG_ID_MAX_LENGTH = 50;

    // Holds the textual representation of this PEPPOL participant id
    private final String value;

    /**
     * Constructs a new instance based upon a match of the following patterns :
     * <ol>
     * <li>{@code xxxx:yyyyyy} - i.e. a 4 digit ICD followed by a ':' followed by the organisationID</li>
     * <li>AB999999999 - i.e. a prefix of at least two characters followed by something</li>
     * </ol>
     *
     * @param participantId participant Id represented as a string
     * @throws InvalidPeppolParticipantException if we are unable to recognize the input as a PEPPOL participant ID
     */
    public ParticipantId(String participantId) {
        value = parse(participantId);
    }

    /**
     * Uses combination of SchemeId and Organisation identifier to create new instance.
     * The Organisation identifier is validated in accordance with the rules of the scheme.
     *
     * @param schemeId
     * @param organisationId
     */
    public ParticipantId(final Icd schemeId, final String organisationId) {

        if (schemeId == null) {
            throw new IllegalArgumentException("SchemeId must be specified with a a valid ISO6523 code.");
        }

        if (organisationId == null) {
            throw new IllegalArgumentException("The organisation id must be specified.");
        }

        if (organisationId.length() > INTERNATION_ORG_ID_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format(
                    "Invalid organisation id. '%s' is longer than %d characters"
                    , organisationId, INTERNATION_ORG_ID_MAX_LENGTH));
        }

        // Formats the organisation identifier in accordance with what PEPPOL expects.
        value = String.format("%s:%s", schemeId.getCode(), organisationId);
    }

    /**
     * Parses the input string assuming it represents an organisation number or PEPPOL participant identifier in one
     * of these forms:
     * <ol>
     * <li>icd +':' + organisation identifier</li>
     * <li>National organisation number with at least two character prefix.</li>
     * </ol>
     * <p>
     * After parsing, the organisation identifier is validated in accordance with the rules of the scheme a
     * validator is found.
     * </p>
     *
     * @param participantId the string representing the participant identifier or organisation identifier
     * @return a string on the form [ISO6523 ICD]:[participantId];
     */
    static String parse(final String participantId) throws InvalidPeppolParticipantException {
        String organisationId = participantId.trim().replaceAll("\\s", ""); // Squeezes out any white spaces
        Icd schemeId = null;

        Matcher matcher = ISO6523_PATTERN.matcher(organisationId);

        if (!matcher.matches())
            throw new InvalidPeppolParticipantException(String.format("ICD not found in '%s'.", participantId));

        // If the representation is in the form xxxx:yyyyyyyyy, we are good
        String icd = matcher.group(1);
        organisationId = matcher.group(2);

        try {
            schemeId = SchemeId.fromISO6523(icd);       // Locates the associated scheme
        } catch (IllegalArgumentException e) {
            // No action.
        }

        if (schemeId == null)
            throw new InvalidPeppolParticipantException("ICD " + icd + " is unknown");

        // Constructs the textual representation of the PEPPOL participant identifier
        return String.format("%s:%s", schemeId.getCode(), organisationId);
    }


    /**
     * Parses the provided participant identifier into a validated instance
     * of {@link ParticipantId}
     *
     * @param participantId The organisation number as xxxx:yyyy or just an organisation number
     * @return validated instance of Participant Id
     */
    public static ParticipantId valueOf(String participantId) {
        return new ParticipantId(parse(participantId.trim()));

    }

    /**
     * Simple syntax verifier, verifies icd prefix + code
     */
    public static boolean isValidParticipantIdentifierPattern(String value) {
        if (value == null) return false;

        Matcher matcher = ISO6523_PATTERN.matcher(value);
        return matcher.find();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantId that = (ParticipantId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

    public ParticipantIdentifier toVefa() {
        return ParticipantIdentifier.of(value, ParticipantIdentifier.DEFAULT_SCHEME);
    }
}
