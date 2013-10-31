/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.inbound.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.developer.JAXWSProperties;
import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolProcessTypeIdAcronym;
import eu.peppol.inbound.soap.PeppolMessageHeaderParser;
import eu.peppol.start.identifier.*;
import org.easymock.EasyMock;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.security.Principal;

import static org.easymock.EasyMock.*;

/**
 * @author steinar
 *         Date: 09.06.13
 *         Time: 22:07
 */
public class MockWebServiceContextModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    PeppolMessageHeaderParser provideMessageHeaderParser() {
        PeppolMessageHeader peppolMessageHeader = new PeppolMessageHeader();

        peppolMessageHeader.setRemoteAccessPointPrincipal(createPrincipal());
        peppolMessageHeader.setChannelId(new ChannelId("CH1"));
        peppolMessageHeader.setDocumentTypeIdentifier(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        peppolMessageHeader.setMessageId(new MessageId("aaaaa"));
        peppolMessageHeader.setPeppolProcessTypeId(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId());
        peppolMessageHeader.setRecipientId(new ParticipantId("9908:976098897"));
        peppolMessageHeader.setSenderId(new ParticipantId("9908:976098897"));

        PeppolMessageHeaderParser messageHeaderParser = createMock(PeppolMessageHeaderParser.class);
        expect(messageHeaderParser.parseSoapHeaders(isA(HeaderList.class))).andReturn(peppolMessageHeader);
        replay(messageHeaderParser);
        return messageHeaderParser;
    }



    @Provides
    WebServiceContext provideWebServiceContext() {
        WebServiceContext mock = EasyMock.createMock(WebServiceContext.class);

        HeaderList headerList = createMock(HeaderList.class);

        MessageContext messageContext = createMock(MessageContext.class);

        expect(mock.getMessageContext()).andReturn(messageContext).anyTimes();
        expect(messageContext.get(JAXWSProperties.INBOUND_HEADER_LIST_PROPERTY)).andReturn(headerList).once();



        Header header = createMock(Header.class);

        expect(headerList.get(isA(QName.class), anyBoolean())).andReturn(header).anyTimes();
        expect(header.getStringContent()).andReturn("dummy").anyTimes();

        HttpServletRequest httpServletRequest = createMock(HttpServletRequest.class);

        expect(messageContext.get(MessageContext.SERVLET_REQUEST)).andReturn(httpServletRequest).once();
        expect(httpServletRequest.getRemoteHost()).andReturn("localhost");

        Subject subject = createSubject();

        expect(messageContext.get("javax.security.auth.Subject")).andReturn(subject);

        EasyMock.replay(mock, messageContext, headerList, header, httpServletRequest);
        return mock;
    }

    private Subject createSubject() {
        Principal p = createPrincipal();

        Subject subject = new Subject();
        subject.getPrincipals().add(p);
        return subject;
    }

    private Principal createPrincipal() {
        return new Principal() {
                @Override
                public String getName() {
                    return "mockPrinicpal";
                }
            };
    }
}
