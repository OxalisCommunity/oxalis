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
package eu.peppol.outbound.soap;

import org.busdox.transport.identifiers._1.DocumentIdentifierType;
import org.busdox.transport.identifiers._1.ParticipantIdentifierType;
import org.busdox.transport.identifiers._1.ProcessIdentifierType;


/**
 * The SOAPHeaderObject class aims to hold header data.
 *
 * @author  Jose Gorvenia Narvaez(jose@alfa1lab.com)
 */
public class SOAPHeaderObject {

    /** BUSDOX Message Identifier. */
    private String messageIdentifier;

    /** BUSDOX Channel Identifier. */
    private String channelIdentifier;

    /** Recipient's BUSDOX Participant Identifier. */
    private ParticipantIdentifierType recipientIdentifier;

    /** Sender's BUSDOX Participant Identifier. */
    private ParticipantIdentifierType senderIdentifier;

    /** BUSDOX Document Identifier. */
    private DocumentIdentifierType documentIdentifier;

    /** BUSDOX Process Identifier. */
    private ProcessIdentifierType processIdentifier;

    /**
     * Gets the Message Identifier.
     *
     * @return the messageIdentifier the Message Identifier.
     */
    public String getMessageIdentifier() {
        return messageIdentifier;
    }

    /**
     * Sets the Message Identifier.
     *
     * @param messageIdentifier the messageIdentifier to set
     */
    public void setMessageIdentifier(String messageIdentifier) {
        this.messageIdentifier = messageIdentifier;
    }

    /**
     * Gets the Channel Identifier.
     *
     * @return the channelIdentifier
     */
    public String getChannelIdentifier() {
        return channelIdentifier;
    }

    /**
     * Sets the Channel Identifier.
     *
     * @param channelIdentifier the channelIdentifier to set
     */
    public void setChannelIdentifier(String channelIdentifier) {
        this.channelIdentifier = channelIdentifier;
    }

    /**
     * Gets the Recipient's Participant Identifier.
     *
     * @return the recipientIdentifier
     */
    public ParticipantIdentifierType getRecipientIdentifier() {
        return recipientIdentifier;
    }

    /**
     * Sets the Recipient's Participant Identifier.
     *
     * @param recipientIdentifier the recipientIdentifier to set
     */
    public void setRecipientIdentifier(ParticipantIdentifierType recipientIdentifier) {
        this.recipientIdentifier = recipientIdentifier;
    }

    /**
     * Gets the Sender's Participant Identifier.
     *
     * @return the senderIdentifier
     */
    public ParticipantIdentifierType getSenderIdentifier() {
        return senderIdentifier;
    }

    /**
     * Sets the Sender's Participant Identifier.
     *
     * @param senderIdentifier the senderIdentifier to set
     */
    public void setSenderIdentifier(ParticipantIdentifierType senderIdentifier) {
        this.senderIdentifier = senderIdentifier;
    }

    /**
     * Gets the Document Identifier.
     *
     * @return the documentIdentifier
     */
    public DocumentIdentifierType getDocumentIdentifier() {
        return documentIdentifier;
    }

    /**
     * Sets the Document Identifier.
     *
     * @param documentIdentifier the documentIdentifier to set
     */
    public void setDocumentIdentifier(DocumentIdentifierType documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    }

    /**
     * Gets the Process Identifier.
     *
     * @return the processIdentifier
     */
    public ProcessIdentifierType getProcessIdentifier() {
        return processIdentifier;
    }

    /**
     * Sets the Process Identifier.
     *
     * @param processIdentifier the processIdentifier to set
     */
    public void setProcessIdentifier(ProcessIdentifierType processIdentifier) {
        this.processIdentifier = processIdentifier;
    }
}

