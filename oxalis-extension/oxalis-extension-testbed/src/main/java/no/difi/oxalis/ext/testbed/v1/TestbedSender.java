package no.difi.oxalis.ext.testbed.v1;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.ext.testbed.v1.jaxb.ErrorType;
import no.difi.oxalis.ext.testbed.v1.jaxb.InboundType;

/**
 * @author erlend
 */
@Singleton
@Slf4j
public class TestbedSender {

    public void send(InboundType inbound) {
        // TODO
    }

    public void send(ErrorType error) {
        log.warn(error.getMessage());

        // TODO
    }
}
