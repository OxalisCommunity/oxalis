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

package eu.peppol.persistence;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.persistence.api.UserName;
import eu.peppol.persistence.api.account.Account;
import eu.peppol.persistence.api.account.CustomerId;

import java.util.Date;

/**
 * Object which shall be used to create complex objects for testing.
 *
 * @author andy
 * @author adam
 * @author thore
 */
public class ObjectMother {

    public static Account getTestAccount(){
        return new Account(new CustomerId(1), "AndyAccount",
                new UserName("sr"), new Date(), getTestPassword(), new AccountId(1), false, true);
    }

    public static Account getAdamsAccount() {
        return new Account(
                new CustomerId(1), "AdamAccount",
                new UserName("adam"), new Date(), getTestPassword(), new AccountId(2), false, true);
    }

    public static Account getThoresAccount() {
        return new Account(new CustomerId(1), "ThoresAccount",
                new UserName("teedjay"), new Date(), getTestPassword(), new AccountId(3), false, true);
    }

    private static String getTestPassword() {
        return "ringo";
    }

    public static ParticipantId getTestParticipantIdForSMPLookup() {
        return WellKnownParticipant.DIFI;
    }

    public static ParticipantId getTestParticipantIdForConsumerReceiver() {
        return new ParticipantId("9999:01029400470");
    }

    public static ParticipantId getTestParticipantId() {
        return new ParticipantId("9908:976098897");
    }

    public static ParticipantId getAdamsParticipantId() {
        return new ParticipantId("9908:988890081");
    }

    public static final PeppolDocumentTypeId getDocumentIdForBisInvoice() {
        return PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol4a:ver2.0::2.1");
    }

}
