package network.oxalis.test.identifier;

import network.oxalis.vefa.peppol.common.model.C1CountryIdentifier;

public class CountryIdentifierExample {
    // The value of the country code MUST be provided according to ISO-3166-1 in the Alpha-2 notation (e.g. “BE” representing Belgium).
    public static final C1CountryIdentifier AU = C1CountryIdentifier.of("AU");
    public static final C1CountryIdentifier BE = C1CountryIdentifier.of("BE");
    public static final C1CountryIdentifier IN = C1CountryIdentifier.of("IN");
    public static final C1CountryIdentifier JP = C1CountryIdentifier.of("JP");
    public static final C1CountryIdentifier NO = C1CountryIdentifier.of("NO");
    public static final C1CountryIdentifier SE = C1CountryIdentifier.of("SE");
    public static final C1CountryIdentifier SG = C1CountryIdentifier.of("SG");

}
