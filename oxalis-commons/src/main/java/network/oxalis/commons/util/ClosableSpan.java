package network.oxalis.commons.util;

import java.io.Closeable;

public interface ClosableSpan extends Closeable {
    void close();
}
