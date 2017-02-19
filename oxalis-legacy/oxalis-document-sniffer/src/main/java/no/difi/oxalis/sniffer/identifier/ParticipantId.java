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

package no.difi.oxalis.sniffer.identifier;

import no.difi.oxalis.sniffer.lang.InvalidPeppolParticipantException;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Steinar Overbeck Cook
 * @author Thore Johnsen
 *         <p>
 *         TODO: introduce the iso6235 ICD as a separate property of the constructor
 * @see SchemeId
 */
public class ParticipantId implements Serializable {

    static final Pattern ISO6523_PATTERN = Pattern.compile("^(\\d{4}):([^\\s]+)$");

    //max length for international organisation number
    static final int INTERNATION_ORG_ID_MAX_LENGTH = 50;

    // Holds the textual representation of this PEPPOL participant id
    private final String peppolParticipantIdValue;

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
        peppolParticipantIdValue = parse(participantId);
    }

    /**
     * Uses combination of SchemeId and Organisation identifier to create new instance.
     * The Organisation identifier is validated in accordance with the rules of the scheme.
     *
     * @param schemeId
     * @param organisationId
     */
    public ParticipantId(final SchemeId schemeId, final String organisationId) {

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
        String oId = schemeId.formatOrganisationId(organisationId);
        peppolParticipantIdValue = schemeId.getIso6523Icd().concat(":").concat(oId);
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
        SchemeId schemeId;

        Matcher matcher = ISO6523_PATTERN.matcher(organisationId);

        // If the representation is in the form xxxx:yyyyyyyyy, we are good
        if (matcher.matches()) {

            String icd = matcher.group(1);
            organisationId = matcher.group(2);
            schemeId = SchemeId.fromISO6523(icd);       // Locates the associated scheme
            if (schemeId == null) {
                throw new InvalidPeppolParticipantException("ICD " + icd + " is unknown");
            }
        } else {
            if (!organisationId.matches(".*\\d.*")) {
                throw new InvalidPeppolParticipantException(String.format(
                        "Organisation identifier must contain digits. Value '%s' is invalid", organisationId));
            }

            // Let's see if we can find the scheme based upon the prefix of the organisation number
            List<SchemeId> matchingSchemes = SchemeId.fuzzyMatchOnOrganisationIdPrefix(organisationId);
            if (matchingSchemes.size() > 1) {
                throw new InvalidPeppolParticipantException(String.format(
                        "Found %s schme identifiers for org. id '%s'.", matchingSchemes.size(), participantId));
            }
            if (matchingSchemes.isEmpty()) {
                throw new InvalidPeppolParticipantException("No matching scheme identifier found for " + participantId);
            }
            schemeId = matchingSchemes.get(0);
        }

        // Formats the Organisation identifier in accordance with PEPPOL's requirements
        organisationId = schemeId.formatOrganisationId(organisationId);

        // Constructs the textual representation of the PEPPOL participant identifier
        return schemeId.getIso6523Icd().concat(":").concat(organisationId);
    }


    /**
     * Parses the provided participant identifier into a validated instance
     * of {@link ParticipantId}
     *
     * @param participantId The organisation number as xxxx:yyyy or just an organisation number
     * @return validated instance of Participant Id
     */
    public static ParticipantId valueOf(String participantId) {
        return new ParticipantId(parse(participantId));

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
        if (peppolParticipantIdValue != null ? !peppolParticipantIdValue.equals(that.peppolParticipantIdValue) :
                that.peppolParticipantIdValue != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return peppolParticipantIdValue != null ? peppolParticipantIdValue.hashCode() : 0;
    }

    public String stringValue() {
        return peppolParticipantIdValue;
    }


    @Override
    public String toString() {
        return peppolParticipantIdValue;
    }

    public ParticipantIdentifier toVefa() {
        return ParticipantIdentifier.of(peppolParticipantIdValue, ParticipantIdentifier.DEFAULT_SCHEME);
    }
}
