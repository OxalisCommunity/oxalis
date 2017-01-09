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

package eu.peppol.identifier;

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
 *         TODO: refactor Norwegian stuff into separate type
 * @see SchemeId
 */
public class ParticipantId implements Serializable {

    static final Pattern ISO6523_PATTERN = Pattern.compile("^(\\d{4}):([^\\s]+)$");

    //max length for international organisation number
    static final int INTERNATION_ORG_ID_MAX_LENGTH = 50;

    private static final String scheme = "iso6523-actorid-upis";

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
            throw new IllegalArgumentException(String.format("Invalid organisation id. '%s' is longer than %d characters", organisationId, INTERNATION_ORG_ID_MAX_LENGTH));
        }

        // Formats the organisation identifier in accordance with what PEPPOL expects.
        String oId = schemeId.formatOrganisationId(organisationId);
        boolean valid = schemeId.validate(oId);
        if (!valid) {
            throw InvalidPeppolParticipantException.forInputString(organisationId);
        }
        peppolParticipantIdValue = schemeId.getIso6523Icd().concat(":").concat(oId);
    }

    public static String getBusDoxScheme() {
        return scheme;
    }

    /**
     * Parses the input string assuming it represents an organisation number or PEPPOL participant identifier in one of these forms:
     * <ol>
     * <li>icd +':' + organisation identifier</li>
     * <li>National organisation number with at least two character prefix.</li>
     * </ol>
     * <p>
     * After parsing, the organisation identifier is validated in accordance with the rules of the scheme a validator is found.
     * </p>
     *
     * @param participantId the string representing the participant identifier or organisation identifier
     * @return a string on the form [ISO6523 ICD]:[participantId];
     */
    static String parse(final String participantId) throws InvalidPeppolParticipantException {

        if (participantId == null) {
            throw InvalidPeppolParticipantException.forInputString("'null'");
        }
        String organisationId = participantId.trim().replaceAll("\\s", "");         // Squeezes out any white spaces
        SchemeId schemeId = null;

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
                throw new InvalidPeppolParticipantException("Organisation identifier must contain digits. [" + organisationId + "] is invalid");
            }

            // Let's see if we can find the scheme based upon the prefix of the organisation number
            List<SchemeId> matchingSchemes = SchemeId.fuzzyMatchOnOrganisationIdPrefix(organisationId);
            if (matchingSchemes.size() > 1) {
                throw new InvalidPeppolParticipantException("Found " + matchingSchemes.size() + " schme identifiers for org. id " + participantId);
            }
            if (matchingSchemes.isEmpty()) {
                throw new InvalidPeppolParticipantException("No matching scheme identifier found for " + participantId);
            }
            schemeId = matchingSchemes.get(0);
        }

        // Formats the Organisation identifier in accordance with PEPPOL's requirements
        organisationId = schemeId.formatOrganisationId(organisationId);

        // Validates the contents of the organisation Id in accordance with the rules of the scheme
        if (!schemeId.validate(organisationId)) {
            throw new InvalidPeppolParticipantException("Validation (modulus check) failed for " + participantId);
        }

        // Constructs the textual representation of the PEPPOL participant identifier
        return schemeId.getIso6523Icd().concat(":").concat(organisationId);
    }


    /** Parses the provided participant identifier into a validated instance
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

    /**
     * Verifies syntax and validates.
     *
     * @deprecated uses try-catch for logic, which is bad!
     */
    public static boolean isValidParticipantIdentifier(String value) {
        try {
            String s = parse(value);
            return true;
        } catch (InvalidPeppolParticipantException e) {
            return false;
        }
    }

    /**
     * Validates Norwegian organisation numbers according to the national rules.
     *
     * @param org organisation number to be checked.
     * @return true if syntactically valid organisation number, false otherwise.
     * @see <a href="http://www.brreg.no/om-oss/samfunnsoppdraget-vart/registera-vare/einingsregisteret/organisasjonsnummeret/">Bronnoysund explanation</a>
     */
    public static boolean isValidNorwegianOrganisationNumber(String org) {
        return SchemeId.NO_ORGNR.validate(org);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantId that = (ParticipantId) o;
        if (peppolParticipantIdValue != null ? !peppolParticipantIdValue.equals(that.peppolParticipantIdValue) : that.peppolParticipantIdValue != null)
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
