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
import org.busdox.transport.identifiers._1.DocumentIdentifierType;
import org.busdox.transport.identifiers._1.ParticipantIdentifierType;
import org.busdox.transport.identifiers._1.ProcessIdentifierType;

import java.util.Date;

/**
 * A MessageMetadata object is used to storage the message addressing data
 * incoming in the SOAP header through a SOAPHeaderObject object.
 *
 * @author Jose Gorvenia Narvaez(jose@alfa1lab.com)
 */
public class MessageMetadata {

    /** The message identifier. */
    private String messageId;

    /** The channel identifier. */
    private String channelId;

    /** The data of creation. */
    private Date createDate;

    /** Sender identifier value. */
    private String senderValue;

    /** Sender identifier scheme. */
    private String senderScheme;

    /** Recipient identifier value. */
    private String recipientValue;

    /** Recipient identifier scheme. */
    private String recipientScheme;

    /** Document identifier value. */
    private String documentIdValue;

    /** Document identifier scheme. */
    private String documentIdScheme;

    /** Process identifier value. */
    private String processIdValue;

    /** Process identifier value. */
    private String processIdScheme;

    /** SOAPHeaderObject object. */
    private SOAPHeaderObject soapHeader;

    /**
     * Set the values of the MessageMetadata object received in a
     * SOAPHeaderObject object.
     *
     * @param soapHeader the message addressing data.
     */
    public MessageMetadata(final SOAPHeaderObject soapHeader) {

        ParticipantIdentifierType sender = soapHeader.getSenderIdentifier();
        this.senderValue = sender.getValue();
        this.senderScheme = sender.getScheme();
        ParticipantIdentifierType recipient = soapHeader.getRecipientIdentifier();
        this.recipientValue = recipient.getValue();
        this.recipientScheme = recipient.getScheme();
        DocumentIdentifierType document =  soapHeader.getDocumentIdentifier();
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

    /**
     * Get message identifier value.
     *
     * @return the messageId the value of the message identifier.
     */
    public final String getMessageId() {
        return messageId;
    }

    /**
     * @param messageId the messageId to set
     */
    public final void setMessageId(final String messageId) {
        this.messageId = messageId;
    }

    /**
     * @return the channelId
     */
    public final String getChannelId() {
        return channelId;
    }

    /**
     * @param channelId the channelId to set
     */
    public final void setChannelId(final String channelId) {
        this.channelId = channelId;
    }

    /**
     * @return the createDate
     */
    public final Date getCreateDate() {
        return new Date(createDate.getTime());
    }

    /**
     * @param createDate the createDate to set
     */
    public final void setCreateDate(final Date createDate) {
        this.createDate = new Date(createDate.getTime());
    }

    /**
     * @return the senderValue
     */
    public final String getSenderValue() {
        return senderValue;
    }

    /**
     * @param senderValue the senderValue to set
     */
    public final void setSenderValue(final String senderValue) {
        this.senderValue = senderValue;
    }

    /**
     * @return the senderScheme
     */
    public final String getSenderScheme() {
        return senderScheme;
    }

    /**
     * @param senderScheme the senderScheme to set
     */
    public final void setSenderScheme(final String senderScheme) {
        this.senderScheme = senderScheme;
    }

    /**
     * @return the recipientValue
     */
    public final String getRecipientValue() {
        return recipientValue;
    }

    /**
     * @param recipientValue the recipientValue to set
     */
    public final void setRecipientValue(final String recipientValue) {
        this.recipientValue = recipientValue;
    }

    /**
     * @return the recipientScheme
     */
    public final String getRecipientScheme() {
        return recipientScheme;
    }

    /**
     * @param recipientScheme the recipientScheme to set
     */
    public final void setRecipientScheme(final String recipientScheme) {
        this.recipientScheme = recipientScheme;
    }

    /**
     * @return the documentIdValue
     */
    public final String getDocumentIdValue() {
        return documentIdValue;
    }

    /**
     * @param documentIdValue the documentIdValue to set
     */
    public final void setDocumentIdValue(final String documentIdValue) {
        this.documentIdValue = documentIdValue;
    }

    /**
     * @return the documentIdScheme
     */
    public final String getDocumentIdScheme() {
        return documentIdScheme;
    }

    /**
     * @param documentIdScheme the documentIdScheme to set
     */
    public final void setDocumentIdScheme(final String documentIdScheme) {
        this.documentIdScheme = documentIdScheme;
    }

    /**
     * @return the processIdValue
     */
    public final String getProcessIdValue() {
        return processIdValue;
    }

    /**
     * @param processIdValue the processIdValue to set
     */
    public final void setProcessIdValue(final String processIdValue) {
        this.processIdValue = processIdValue;
    }

    /**
     * @return the processIdScheme
     */
    public final String getProcessIdScheme() {
        return processIdScheme;
    }

    /**
     * @param processIdScheme the processIdScheme to set
     */
    public final void setProcessIdScheme(final String processIdScheme) {
        this.processIdScheme = processIdScheme;
    }

    /**
     * @return the soapHeader
     */
    public final SOAPHeaderObject getSoapHeader() {
        return soapHeader;
    }

    /**
     * @param soapHeader the soapHeader to set
     */
    public final void setSoapHeader(final SOAPHeaderObject soapHeader) {
        this.soapHeader = soapHeader;
    }
}
