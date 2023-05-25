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

package network.oxalis.outbound.lookup;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.lookup.LookupService;
import network.oxalis.commons.guice.GuiceModuleLoader;
import network.oxalis.vefa.peppol.common.model.*;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = GuiceModuleLoader.class)
public class CachedLookupServiceTest {

    private static ParticipantIdentifier participant = ParticipantIdentifier.of("0208:0871221633");

    private static DocumentTypeIdentifier documenttype = DocumentTypeIdentifier.of(
            "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##" +
                    "urn:cen.eu:en16931:2017#compliant#" +
                    "urn:fdc:peppol.eu:2017:poacc:billing:3.0" +
                    "::2.1", DocumentTypeIdentifier.BUSDOX_DOCID_QNS_SCHEME);

    private static ProcessIdentifier process = ProcessIdentifier.of("urn:fdc:peppol.eu:2017:poacc:billing:01:1.0");

    @Inject
    @Named("cached")
    private LookupService lookupService;

    @Test
    public void simple() throws Exception {
        Endpoint endpoint = lookupService.lookup(Header.newInstance()
                .receiver(participant)
                .documentType(documenttype)
                .process(process));

        Assert.assertNotNull(endpoint);
    }

    @Test
    public void simpleCached() throws Exception {
        Endpoint endpoint1 = lookupService.lookup(Header.newInstance()
                .receiver(participant)
                .documentType(documenttype)
                .process(process));

        Endpoint endpoint2 = lookupService.lookup(Header.newInstance()
                .receiver(participant)
                .documentType(documenttype)
                .process(process));

        Assert.assertSame(endpoint1, endpoint2);
    }

    @Test(expectedExceptions = OxalisTransmissionException.class)
    public void triggerException() throws Exception {
        lookupService.lookup(Header.newInstance()
                .receiver(ParticipantIdentifier.of("9908:---"))
                .documentType(documenttype)
                .process(process));
    }

    @Test
    public void simpleHeaderStub() {
        CachedLookupService.HeaderStub headerStub = new CachedLookupService.HeaderStub(Header.newInstance()
                .receiver(participant)
                .documentType(documenttype)
                .process(process));

        Assert.assertTrue(headerStub.equals(new CachedLookupService.HeaderStub(Header.newInstance()
                .receiver(participant)
                .documentType(documenttype)
                .process(process))));
        Assert.assertFalse(headerStub.equals(new CachedLookupService.HeaderStub(Header.newInstance()
                .receiver(ParticipantIdentifier.of("9908:---"))
                .documentType(documenttype)
                .process(process))));
        Assert.assertFalse(headerStub.equals(new CachedLookupService.HeaderStub(Header.newInstance()
                .receiver(participant)
                .documentType(DocumentTypeIdentifier.of("---"))
                .process(process))));
        Assert.assertFalse(headerStub.equals(new CachedLookupService.HeaderStub(Header.newInstance()
                .receiver(participant)
                .documentType(documenttype)
                .process(ProcessIdentifier.of("---")))));
    }
}
