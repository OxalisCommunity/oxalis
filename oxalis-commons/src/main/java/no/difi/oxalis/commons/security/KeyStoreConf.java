package no.difi.oxalis.commons.security;

import no.difi.oxalis.api.settings.DefaultValue;
import no.difi.oxalis.api.settings.Path;
import no.difi.oxalis.api.settings.Title;

/**
 * @author erlend
 * @since 4.0.0
 */
@Title("Key store")
public enum KeyStoreConf {

    @Path("oxalis.keystore.path")
    @DefaultValue("oxalis-keystore.jks")
    PATH,

    @Path("oxalis.keystore.password")
    @DefaultValue("changeit")
    PASSWORD,

    @Path("oxalis.keystore.key.alias")
    @DefaultValue("ap")
    KEY_ALIAS,

    @Path("oxalis.keystore.key.password")
    @DefaultValue("changeit")
    KEY_PASSWORD

}
