package network.oxalis.outbound.lookup;

import network.oxalis.api.settings.DefaultValue;
import network.oxalis.api.settings.Path;
import network.oxalis.api.settings.Title;

@Title("Lookup")
public enum LookupConf {

    @Path("oxalis.pint.wildcard.migration.phase")
    @DefaultValue("0")
    PINT_WILDCARD_MIGRATION_PHASE;

}