/*
 * Version: MPL 1.1/EUPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Copyright The PEPPOL project (http://www.peppol.eu)
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL
 * (the "Licence"); You may not use this work except in compliance
 * with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * If you wish to allow use of your version of this file only
 * under the terms of the EUPL License and not to allow others to use
 * your version of this file under the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and
 * other provisions required by the EUPL License. If you do not delete
 * the provisions above, a recipient may use your version of this file
 * under either the MPL or the EUPL License.
 */
package eu.peppol.inbound.metadata;

import eu.peppol.outbound.soap.SOAPHeaderObject;

import java.util.Date;

import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;

/**
 * A MessageMetadata object is used to storage the message addressing data
 * incoming in the SOAP header through a SOAPHeaderObject object.
 *
 * @author Jose Gorvenia Narvaez(jose@alfa1lab.com)
 */
public class MessageMetadata {

    private String messageId;
    private String channelId;
    private Date createDate;
    private String senderValue;
    private String senderScheme;
    private String recipientValue;
    private String recipientScheme;
    private String documentIdValue;
    private String documentIdScheme;
    private String processIdValue;
    private String processIdScheme;
    private SOAPHeaderObject soapHeader;

    public MessageMetadata() {
    }

    public MessageMetadata(SOAPHeaderObject soapHeader) {

        ParticipantIdentifierType sender = soapHeader.getSenderIdentifier();
        this.senderValue = sender.getValue();
        this.senderScheme = sender.getScheme();
        ParticipantIdentifierType recipient = soapHeader.getRecipientIdentifier();
        this.recipientValue = recipient.getValue();
        this.recipientScheme = recipient.getScheme();
        DocumentIdentifierType document = soapHeader.getDocumentIdentifier();
        this.documentIdValue = document.getValue();
        this.documentIdScheme = document.getScheme();
        ProcessIdentifierType process = soapHeader.getProcessIdentifier();
        this.processIdValue = process.getValue();
        this.processIdScheme = process.getScheme();
        this.messageId = soapHeader.getMessageIdentifier();
        this.channelId = soapHeader.getChannelIdentifier();
        this.createDate = new Date();
        this.soapHeader = soapHeader;
    }

    public final String getMessageId() {
        return messageId;
    }

    public final void setMessageId(final String messageId) {
        this.messageId = messageId;
    }

    public final String getChannelId() {
        return channelId;
    }

    public final void setChannelId(final String channelId) {
        this.channelId = channelId;
    }

    public final Date getCreateDate() {
        return new Date(createDate.getTime());
    }

    public final void setCreateDate(final Date createDate) {
        this.createDate = new Date(createDate.getTime());
    }

    public final String getSenderValue() {
        return senderValue;
    }

    public final void setSenderValue(final String senderValue) {
        this.senderValue = senderValue;
    }

    public final String getSenderScheme() {
        return senderScheme;
    }

    public final void setSenderScheme(final String senderScheme) {
        this.senderScheme = senderScheme;
    }

    public final String getRecipientValue() {
        return recipientValue;
    }

    public final void setRecipientValue(final String recipientValue) {
        this.recipientValue = recipientValue;
    }

    public final String getRecipientScheme() {
        return recipientScheme;
    }

    public final void setRecipientScheme(final String recipientScheme) {
        this.recipientScheme = recipientScheme;
    }

    public final String getDocumentIdValue() {
        return documentIdValue;
    }

    public final void setDocumentIdValue(final String documentIdValue) {
        this.documentIdValue = documentIdValue;
    }

    public final String getDocumentIdScheme() {
        return documentIdScheme;
    }

    public final void setDocumentIdScheme(final String documentIdScheme) {
        this.documentIdScheme = documentIdScheme;
    }

    public final String getProcessIdValue() {
        return processIdValue;
    }

    public final void setProcessIdValue(final String processIdValue) {
        this.processIdValue = processIdValue;
    }

    public final String getProcessIdScheme() {
        return processIdScheme;
    }

    public final void setProcessIdScheme(final String processIdScheme) {
        this.processIdScheme = processIdScheme;
    }

    public final SOAPHeaderObject getSoapHeader() {
        return soapHeader;
    }

    public final void setSoapHeader(final SOAPHeaderObject soapHeader) {
        this.soapHeader = soapHeader;
    }
}
