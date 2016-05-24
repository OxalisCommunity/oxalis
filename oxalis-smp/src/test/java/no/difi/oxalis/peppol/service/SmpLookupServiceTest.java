/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package no.difi.oxalis.peppol.service;

import eu.peppol.identifier.Endpoint;
import eu.peppol.sbdh.SbdhMessageHeader;
import no.difi.oxalis.smp.service.SmpLookupService;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.net.URI;

public class SmpLookupServiceTest {

    private JAXBContext jaxbContext;
    private SmpLookupService smpLookupService;

    @BeforeClass
    public void beforeClass() throws Exception {
        jaxbContext = JAXBContext.newInstance(StandardBusinessDocumentHeader.class);
        smpLookupService = new SmpLookupService();
    }

    @Test(enabled = false)
    public void validInvoice() throws Exception {
        StandardBusinessDocumentHeader header = loadResourceHeader("/sbdh/header-invoice.xml");
        Endpoint endpoint = smpLookupService.getEndpoint(new SbdhMessageHeader(header), "busdox-transport-as2-ver1p0");

        Assert.assertEquals(endpoint.getAddress(), URI.create("https://aksesspunkt.difi.no/as2"));
    }

    private StandardBusinessDocumentHeader loadResourceHeader(String filename) throws Exception {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
                new StreamSource(getClass().getResourceAsStream("/sbdh/header-invoice.xml")),
                StandardBusinessDocumentHeader.class
        ).getValue();
    }

}
