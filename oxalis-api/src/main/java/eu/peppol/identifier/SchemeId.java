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

package eu.peppol.identifier;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Provides a binding between the attributes schemeAgencyId and the corresponding ISO6523 prefix (ICD).
 * The ENUM is taken from Policy for use of Identifiers version 3.0 dated 2014-02-03.
 * The ICD's should be 4 digits, a list can be found : http://www.oid-info.com/doc/ICD-list.pdf
 * <p>
 * Possible improvements are:
 * <ul>
 * <li>Add an attribute with the literal prefix of the organisation identifiers for each scheme. This would make it easier to identify which scheme
 * an organisation identifier belongs to. This could be combined with a regexp</li>
 * </ul>
 *
 * @author andy
 * @author steinar
 * @author thore
 */
public enum SchemeId {

    // @Deprecated AT_CID("AT:CID", "9916"),
    // @Deprecated EU_VAT("EU:VAT", "9912"),
    AD_VAT("AD:VAT", "9922"),
    AL_VAT("AL:VAT", "9923"),
    AT_GOV("AT:GOV", "9915"),
    AT_KUR("AT:KUR", "9919"),
    AT_VAT("AT:VAT", "9914"),
    BA_VAT("BA:VAT", "9924"),
    BE_VAT("BE:VAT", "9925"),
    BG_VAT("BG:VAT", "9926"),
    CH_VAT("CH:VAT", "9927"),
    CY_VAT("CY:VAT", "9928"),
    CZ_VAT("CZ:VAT", "9929"),
    DE_VAT("DE:VAT", "9930"),
    DK_CPR("DK:CPR", "9901"),
    DK_CVR("DK:CVR", "9902"),
    DK_P("DK:P", "0096"),
    DK_SE("DK:SE", "9904"),
    DK_VANS("DK:VANS", "9905"),
    DU_S("DUNS", "0060"),
    EE_VAT("EE:VAT", "9931"),
    ES_VAT("ES:VAT", "9920"),
    EU_REID("EU:REID", "9913"),
    FI_OVT("FI:OVT", "0037"),
    FR_SIRENE("FR:SIRENE", "0002"), // Candidate for matching using prefix of "SIRENE"?
    FR_SIRET("FR:SIRET", "0009"),   // Candidate for matching using prefix of "SIRET"?
    GB_VAT("GB:VAT", "9932"),
    GL_("GLN", "0088"),
    GR_VAT("GR:VAT", "9933"),
    HR_VAT("HR:VAT", "9934"),
    HU_VAT("HU:VAT", "9910"),
    IB_N("IBAN", "9918"),
    IE_VAT("IE:VAT", "9935"),
    IS_KT("IS:KT", "9917"),
    IT_CF("IT:CF", "9907"),
    IT_FTI("IT:FTI", "0097"),
    IT_IPA("IT:IPA", "9921"),
    IT_SECETI("IT:SECETI", "0142"),
    IT_SIA("IT:SIA", "0135"),
    IT_VAT("IT:VAT", "9906"),
    LI_VAT("LI:VAT", "9936"),
    LT_VAT("LT:VAT", "9937"),
    LU_VAT("LU:VAT", "9938"),
    LV_VAT("LV:VAT", "9939"),
    MC_VAT("MC:VAT", "9940"),
    ME_VAT("ME:VAT", "9941"),
    MK_VAT("MK:VAT", "9942"),
    MT_VAT("MT:VAT", "9943"),
    NL_ION("NL:OIN", "9954"), /* was wrongly noted as NL:ION in the peppol document */
    NL_KVK("NL:KVK", "0106"),
    NL_VAT("NL:VAT", "9944"),
    NO_ORGNR("NO:ORGNR", "9908"),
    // @Deprecated NO_VAT("NO:VAT", "9909") ,
    PL_VAT("PL:VAT", "9945"),
    PT_VAT("PT:VAT", "9946"),
    RO_VAT("RO:VAT", "9947"),
    RS_VAT("RS:VAT", "9948"),
    SE_ORGNR("SE:ORGNR", "0007"),
    SE_VAT("SE:VAT", "9955"),
    SI_VAT("SI:VAT", "9949"),
    SK_VAT("SK:VAT", "9950"),
    SM_VAT("SM:VAT", "9951"),
    TR_VAT("TR:VAT", "9952"),
    VA_VAT("VA:VAT", "9953"),
    BE_CBE("BE:CBE", "9956"), /* Belgian Crossroad Bank of Enterprises to the list as BE:CBE  9956 */
    ZZ_("ZZZ", "9999");

    final String schemeId;
    final String iso6523Icd;

    SchemeId(String schemeId, String iso6523Icd) {
        this.schemeId = schemeId;
        this.iso6523Icd = iso6523Icd;
    }

    /**
     * Tries to find the Party id with the given schemeId
     * e.g. "ES:VAT" --&gt; ES_VAT
     *
     * @param schemeId textual representation of scheme, i.e. NO_ORGNR
     * @return instance of SchemeId if found
     * @throws IllegalStateException if not found, i.e. unknown scheme
     */
    public static SchemeId parse(String schemeId) {
        if (schemeId == null)
            return null;

        for (SchemeId entry : values()) {
            if (entry.schemeId.equalsIgnoreCase(schemeId)) {
                return entry;
            }
        }
        throw new IllegalStateException("schemeID '" + schemeId + "' is unknown");
    }

    /**
     * Tries to find the Party id from the ISO652 code
     * e.g. "9919" --&gt; AT_KUR
     *
     * @param code
     * @return the scheme id if found null otherwise.
     */
    public static SchemeId fromISO6523(String code) {
        if (code == null) {
            return null;
        }
        for (SchemeId schemeId : values()) {
            if (schemeId.iso6523Icd.equalsIgnoreCase(code)) {
                return schemeId;
            }
        }
        return null;
    }

    /**
     * Attempts to locate instances of SchemeId having the same prefix as the organisation identifier.
     * I.e. {@code NO976098897MVA} has the same prefix has {@code NO:ORGNR}.
     * <p>
     * This method will split the SchemeId using ':' as the delimiter and select the first part (the prefix).
     * All scheme identifiers for which the organisation identifier starts with the schme id prefix, will be chosen.
     * <p>
     * This method could be improved by adding another attribute to the enum, specifying the official prefix, rather than using the
     * first part of the agency code.
     *
     * @param organisationId The organisation number to check
     * @return a list of scheme identifiers matching the start of the organisation identifier. The list is empty if nothing was found.
     */
    public static List<SchemeId> fuzzyMatchOnOrganisationIdPrefix(String organisationId) {
        List<SchemeId> matchingSchemes = Stream.of(SchemeId.values())
                // Filters the scheme identifiers having the same prefix as the organisation number
                .filter(schemeId -> {
                    String prefix = schemeId.getSchemeId().split(":")[0];
                    return organisationId.toUpperCase().startsWith(prefix);
                })
                .collect(toList());

        return matchingSchemes;
    }

    public String getSchemeId() {
        return schemeId;
    }

    public String getIso6523Icd() {
        return iso6523Icd;
    }

    /**
     * Allows a specific PartyId implementation to format the organisationId
     * correctly in accordance with what is required by PEPPOL.
     * <p>
     * The norwegian Organisation number can be postfixed with MVA or prefixed with NO
     * <p>
     * e.g. 987654321MVA is valid as is NO987654321MVA
     *
     * @param organisationId
     * @return
     */
    public String formatOrganisationId(String organisationId) {
        return organisationId;
    }

    /**
     * Validates an organisation id according to the rules of the scheme. Must be overridden per scheme
     *
     * @param organisationId
     * @return
     */
    public boolean validate(String organisationId) {
        return true;
    }
}