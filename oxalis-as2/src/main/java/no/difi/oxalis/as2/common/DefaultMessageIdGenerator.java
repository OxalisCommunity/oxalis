/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.as2.common;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.api.util.Type;
import no.difi.oxalis.as2.api.MessageIdGenerator;

import javax.mail.internet.InternetAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author erlend
 */
@Singleton
@Type("default")
public class DefaultMessageIdGenerator implements MessageIdGenerator {

    private String hostname;

    private AtomicLong atomicLong = new AtomicLong();

    @Inject
    public DefaultMessageIdGenerator(Settings<As2Conf> settings) {
        try {
            hostname = settings.getString(As2Conf.HOSTNAME);
            if (hostname.trim().isEmpty())
                hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to get local hostname.", e);
        }
    }

    @Override
    public String generate(TransmissionRequest transmissionRequest) {
        return new StringBuilder()
                .append('<')
                .append(System.currentTimeMillis())
                .append('.')
                .append(atomicLong.incrementAndGet())
                .append('.')
                .append(transmissionRequest.hashCode())
                .append('.')
                .append("Oxalis")
                .append('@')
                .append(hostname)
                .append('>')
                .toString();
    }
}
