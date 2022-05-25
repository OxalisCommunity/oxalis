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

import network.oxalis.vefa.peppol.icd.Icds;
import network.oxalis.vefa.peppol.icd.api.Icd;
import network.oxalis.vefa.peppol.icd.code.PeppolIcd;

/**
 * Provides a binding between the attributes schemeAgencyId and the corresponding ISO6523 prefix (ICD).
 * The ENUM is taken from Policy for use of Identifiers version 3.0 dated 2014-02-03.
 * The ICD's should be 4 digits, a list can be found : http://www.oid-info.com/doc/ICD-list.pdf
 * <p>
 * Possible improvements are:
 * <ul>
 * <li>Add an attribute with the literal prefix of the organisation identifiers for each scheme.
 * This would make it easier to identify which scheme an organisation identifier belongs to. This could be
 * combined with a regexp</li>
 * </ul>
 *
 * @author andy
 * @author steinar
 * @author thore
 */
public class SchemeId {

    private static final Icds ICDS = Icds.of(PeppolIcd.values());

    /**
     * Tries to find the Party id with the given schemeId
     * e.g. "ES:VAT" --&gt; ES_VAT
     *
     * @param schemeId textual representation of scheme, i.e. NO:ORGNR
     * @return instance of SchemeId if found
     * @throws IllegalStateException if not found, i.e. unknown scheme
     */
    public static Icd parse(String schemeId) {
        return ICDS.findByIdentifier(schemeId);
    }

    /**
     * Tries to find the Party id from the ISO652 code
     * e.g. "9919" --&gt; AT_KUR
     *
     * @param code
     * @return the scheme id if found null otherwise.
     */
    public static Icd fromISO6523(String code) {
        return ICDS.findByCode(code);
    }
}
