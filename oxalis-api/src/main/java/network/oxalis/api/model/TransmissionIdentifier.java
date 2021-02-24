/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package network.oxalis.api.model;

import network.oxalis.vefa.peppol.common.model.AbstractSimpleIdentifier;

import java.io.Serializable;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author erlend
 */
public class TransmissionIdentifier extends AbstractSimpleIdentifier implements Serializable {

    private static final long serialVersionUID = 5280858533226027168L;

    private static final Pattern RFC2822 = Pattern.compile("^<(.+?)>$");

    public static TransmissionIdentifier generateUUID() {
        return of(UUID.randomUUID().toString());
    }

    public static TransmissionIdentifier of(String value) {
        return new TransmissionIdentifier(value);
    }

    public static TransmissionIdentifier fromHeader(String value) {
        Matcher matcher = RFC2822.matcher(value);
        if (matcher.matches())
            return of(matcher.group(1));

        return of(value);
    }

    private TransmissionIdentifier(String value) {
        super(value);
    }

}
