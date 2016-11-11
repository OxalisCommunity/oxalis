/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.identifier.orgid;

import eu.peppol.identifier.InvalidPeppolParticipantException;
import eu.peppol.identifier.SchemeId;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles parsing, validation and iso6523 formatting for Norwegian organisation numbers.
 *
 * @author steinar
 *         Date: 09.11.2016
 *         Time: 09.50
 */
public class NorwegianOrganisationIdParser implements OrganisationIdParser {

    static final Pattern NO_ORG_NUM_PATTERN = Pattern.compile("^(?:NO)?\\s*(\\d{9})\\s*(?:MVA)?$");

    /**
     * Formats the input organisation Id into the syntax required by peppol.
     * I.e. strip off "NO" prefix and "MVA" suffix
     * @param schemeId
     * @param organisationId
     * @return
     */
    @Override
    public String format(SchemeId schemeId, String organisationId) throws InvalidPeppolParticipantException {
        //Clean up Norwegian NO VAT strings
        Matcher matcher = NO_ORG_NUM_PATTERN.matcher(organisationId);
        if (matcher.matches()) {
            String orgNo = matcher.group(1);

            return orgNo;

        }
        throw new InvalidPeppolParticipantException("Unable to parse " + organisationId + " into valid PEPPOL organisation Id");
    }


    /**
     * Validates Norwegian organisation numbers according to the national rules.
     *
     * @param organisationId organisation number to be checked.
     * @return true if syntactically valid organisation number, false otherwise.
     * @see <a href="http://www.brreg.no/om-oss/samfunnsoppdraget-vart/registera-vare/einingsregisteret/organisasjonsnummeret/">Bronnoysund explanation</a>
     */
    @Override
    public boolean validate(SchemeId schemeId, String organisationId) {
        if (organisationId == null || organisationId.length() != 9 || !Character.isDigit(organisationId.charAt(8))) {
            return false;
        }

        int actualCheckDigit = organisationId.charAt(8) - '0'; // From character representation to integer
        List<Integer> weights = Arrays.asList(3, 2, 7, 6, 5, 4, 3, 2);
        int sum = 0;

        for (int i = 0; i < 8; i++) {
            char next = organisationId.charAt(i);

            if (!Character.isDigit(next)) {
                return false;
            }

            int digit = (int) next - '0'; // From character representation to integer
            sum += digit * weights.get(i);
        }

        int modulus = sum % 11;

        /** don't subtract from length if the modulus is 0 */
        if ((modulus == 0) && (actualCheckDigit == 0)) {
            return true;
        }

        // If modulus is  10 (remainder is 1), the check digit shall be replaced with '-', which is illegal and the test below
        // will fail
        int calculatedCheckDigit = 11 - modulus;
        return actualCheckDigit == calculatedCheckDigit;
    }
}
