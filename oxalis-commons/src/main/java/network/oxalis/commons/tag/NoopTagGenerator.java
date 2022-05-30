package network.oxalis.commons.tag;

import network.oxalis.api.model.Direction;
import network.oxalis.api.tag.Tag;
import network.oxalis.api.tag.TagGenerator;
import network.oxalis.api.util.Type;

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
