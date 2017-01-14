package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.MessageSender;
import no.difi.vefa.peppol.common.model.TransportProfile;
import no.difi.vefa.peppol.mode.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class MessageSenderFactory {

    private static Logger logger = LoggerFactory.getLogger(MessageSenderFactory.class);

    private Injector injector;

    private Map<TransportProfile, Config> configMap;

    private List<TransportProfile> prioritizedTransportProfiles;

    @Inject
    public MessageSenderFactory(Injector injector, Mode mode) {
        this.injector = injector;
        Config config = mode.getConfig();

        configMap = config.getObject("transport").keySet().stream()
                .map(key -> config.getConfig(String.format("transport.%s", key)))
                .collect(Collectors.toMap(c -> TransportProfile.of(c.getString("profile")), Function.identity()));

        prioritizedTransportProfiles = Collections.unmodifiableList(configMap.values().stream()
                .filter(o -> !o.hasPath("enabled") || o.getBoolean("enabled"))
                .sorted((o1, o2) -> Integer.compare(o2.getInt("weight"), o1.getInt("weight")))
                .map(o -> o.getString("profile"))
                .map(TransportProfile::of)
                .collect(Collectors.toList()));

        logger.info("Prioritized list of transport profiles: {}", prioritizedTransportProfiles);
    }

    public List<TransportProfile> getPrioritizedTransportProfiles() {
        return prioritizedTransportProfiles;
    }

    public String getSender(TransportProfile transportProfile) throws OxalisTransmissionException {
        if (!configMap.containsKey(transportProfile))
            throw new OxalisTransmissionException(String.format("Transport protocol '%s' not supported.", transportProfile.getValue()));

        return configMap.get(transportProfile).getString("sender");
    }

    public MessageSender getMessageSender(TransportProfile transportProfile) throws OxalisTransmissionException {
        return injector.getInstance(
                Key.get(MessageSender.class, Names.named(getSender(transportProfile))));
    }
}
