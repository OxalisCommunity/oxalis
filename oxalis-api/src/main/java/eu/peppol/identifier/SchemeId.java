package eu.peppol.identifier;

/**
 * Provides a binding between the attributes schemeAgencyId and the corresponding ISO6523 prefix (ICD).
 * <p/>
 * The current understanding is: under the scheme identified by the code "82" there multiple schema agencies, which
 * are identified using letters like for instance NO:ORGNR.
 * <p/>
 * User: andy and steinar
 * Date: 2/7/12
 * Time: 3:11 PM
 */
public enum SchemeId {
    FR_SIRET("FR:SIRET", "0002"),
    SE_ORGNR("SE:ORGNR", "0007"),

    FI_OVT("FI:OVT", "0037"),

    DUNS("DUNS", "0062"),
    GLN("GLN", "0088"),

    DK_P("DK:P", "0096"),

    IT_FTI("IT:FTI", "0097"),
    IT_SIA("IT:SIA", "0135"),
    IT_SECETI("IT:SECETI", "0142"),

    DK_CPR("DK:CPR", "9901"),
    DK_CVR("DK:CVR", "9902"),
    DK_SE("DK:SE", "9904"),
    DK_VANS("DK:VANS", "9905"),

    IT_VAT("IT:VAT", "9906"),
    IT_CF("IT:CF", "9907"),

    NO_ORGNR("NO:ORGNR", "9908"),

    NO_VAT("NO:VAT", "9908"),

    HU_VAT("HU:VAT", "9910"),

    EU_VAT("EU:VAT", "9912"),
    EU_REID("EU:REID", "9913"),

    AT_VAT("AT:VAT", "9914"),
    AT_GOV("AT:GOV", "9915"),

    @Deprecated
    AT_CID("AT:CID", "9916"),

    IS_KT("IS:KT", "9917"),

    IBAN("IBAN", "9918"),

    AT_KUR("AT:KUR", "9919"),

    ES_VAT("ES:VAT", "9920"),
    IT_IPA("IT:IPA", "9921"),
    AD_VAT("AD:VAT", "9922"),
    AL_VAT("AL:VAT", "9923"),
    BA_VAT("BA:VAT", "9924"),
    BE_VAT("BE:VAT", "9925"),
    BG_VAT("BG_VAT", "9926"),
    CH_VAT("CH:VAT", "9927"),
    CY_VAT("CY:VAT", "9928"),
    CZ_VAT("CZ:VAT", "9929"),
    DE_VAT("DE:VAT", "9930"),
    EE_VAT("EE:VAT", "9931"),
    GB_VAT("GB:VAT", "9932"),
    GR_VAT("GR:VAT", "9933"),
    HR_VAT("HR:VAT", "9934"),
    IE_VAT("IE:VAT", "9935"),
    LI_VAT("LI:VAT", "9936"),
    LT_VAT("LT:VAT", "9937"),
    LU_VAT("LU:VAT", "9938"),
    LV_VAT("LV:VAT", "9939"),
    MC_VAT("MC:VAT", "9940"),
    ME_VAT("ME:VAT", "9941"),
    MK_VAT("MK:VAT", "9942"),
    MT_VAT("MT:VAT", "9943"),
    NL_VAT("NL:VAT", "9944"),
    PL_VAT("PL:VAT", "9945"),
    PT_VAT("PT:VAT", "9946"),
    RO_VAT("RO:VAT", "9947"),
    RS_VAT("RS:VAT", "9948"),
    SI_VAT("SI:VAT", "9949"),
    SK_VAT("SK:VAT", "9950"),
    SM_VAT("SM:VAT", "9951"),
    TR_VAT("TR:VAT", "9952"),
    VA_VAT("VA:VAT", "9953");



    final String schemeId;
    final String iso6523Icd;

    SchemeId(String schemeId, String iso6523Icd) {
        this.schemeId = schemeId;
        this.iso6523Icd = iso6523Icd;
    }

    public String getSchemeId() {
        return schemeId;
    }

    public String getIso6523Icd() {
        return iso6523Icd;
    }

    /**
     * Allows a specific PartyId implementation to format the organisationId
     * correctly.
     * <p/>
     * The norwegian Organisation number can be postfixed with MVA or prefixed with NO
     * <p/>
     * e.g. 987654321MVA is valid as is NO987654321MVA
     *
     * @param organisationId
     * @return
     */
    public String formatOrganisationId(String organisationId) {
        return organisationId;
    }

    /**
     * Tries to find the Party id with the given schemeId
     * e.g. "ES:VAT" --> ES_VAT
     *
     * @param schemeId
     * @return the PartyId if found, null otherwise
     */
    public static SchemeId parse(String schemeId) {
        if (schemeId == null)
            return null;

        for (SchemeId partyId : values()) {
            if (partyId.schemeId.equalsIgnoreCase(schemeId)) {
                return partyId;
            }
        }
        throw new IllegalStateException("schemeID '" + schemeId + "' is unknown");
    }

    /**
     * Tries to find the Party id from the ISO652 code
     * e.g. "9919" --> AT_KUR
     *
     * @param code
     * @return the party id if found null otherwise.
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
}