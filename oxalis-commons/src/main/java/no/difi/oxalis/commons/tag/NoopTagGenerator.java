package no.difi.oxalis.commons.tag;

import no.difi.oxalis.api.model.Direction;
import no.difi.oxalis.api.tag.Tag;
import no.difi.oxalis.api.tag.TagGenerator;
import no.difi.oxalis.api.util.Type;

/**
 * @author erlend
 * @since 4.0.2
 */
@Type("noop")
public class NoopTagGenerator implements TagGenerator {

    @Override
    public Tag generate(Direction direction) {
        return Tag.NONE;
    }
}
