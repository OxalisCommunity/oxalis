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

package network.oxalis.test.identifier;

import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;

/**
 * @author steinar
 *         Date: 05.11.13
 *         Time: 14:00
 */
public class WellKnownParticipant {

    public static final ParticipantIdentifier U4_TEST = ParticipantIdentifier.of("9908:810017902");


    public static final ParticipantIdentifier DIFI = ParticipantIdentifier.of("9908:991825827");

    /**
     * Use this in test mode
     */
    public static final ParticipantIdentifier DIFI_TEST = ParticipantIdentifier.of("9908:810418052");

    /**
     * Random endpoint in test mode
     */
    public static final ParticipantIdentifier RANDOM_TEST = ParticipantIdentifier.of("0208:0871221633");

    /**
     * Old organisation number for Balder Treindustri
     */
    public static final ParticipantIdentifier DUMMY = ParticipantIdentifier.of("9908:976098897");

}
