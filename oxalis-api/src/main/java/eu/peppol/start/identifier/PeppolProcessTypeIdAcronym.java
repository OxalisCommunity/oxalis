package eu.peppol.start.identifier;

/**
 * Acronyms for the various PEPPOL processes. Makes life a little easier, as the
 * PeppolProcessTypeId only represents a type safe value of any kind of string.
 *
 * According to Policy 16:
 * <em></em>PEPPOL processes are identified by the respective BII processes. The process identifier has to match the BII profile ID.</em>
 *
 * @author Steinar Overbeck Cook
 *
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 19:18
 *
 * @see "Tranport Policy for using Identifiers"
 */
public enum PeppolProcessTypeIdAcronym {


    ORDER_ONLY("urn:www.cenbii.eu:profile:bii03:ver1.0"),
    INVOICE_ONLY("urn:www.cenbii.eu:profile:bii04:ver1.0"),
    PROCUREMENT("urn:www.cenbii.eu:profile:bii06:ver1.0");

    private PeppolProcessTypeId peppolProcessTypeId;


    private PeppolProcessTypeIdAcronym(String profileId) {
        peppolProcessTypeId = PeppolProcessTypeId.valueOf(profileId);
    }

    public PeppolProcessTypeId getPeppolProcessTypeId() {
        return peppolProcessTypeId;
    }

    @Override
    public String toString() {
        return peppolProcessTypeId.toString();
    }
}
