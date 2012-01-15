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

import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;

/**
 * The SOAPHeaderObject class aims to hold header data.
 *
 * @author  Jose Gorvenia Narvaez(jose@alfa1lab.com)
 */
public class SoapHeader {

    private String messageIdentifier;
    private String channelIdentifier;
    private ParticipantIdentifierType recipientIdentifier;
    private ParticipantIdentifierType senderIdentifier;
    private DocumentIdentifierType documentIdentifier;
    private ProcessIdentifierType processIdentifier;

    public String getMessageIdentifier() {
        return messageIdentifier;
    }

    public void setMessageIdentifier(String messageIdentifier) {
        this.messageIdentifier = messageIdentifier;
    }

    public String getChannelIdentifier() {
        return channelIdentifier;
    }

    public void setChannelIdentifier(String channelIdentifier) {
        this.channelIdentifier = channelIdentifier;
    }

    public ParticipantIdentifierType getRecipientIdentifier() {
        return recipientIdentifier;
    }

    public void setRecipientIdentifier(ParticipantIdentifierType recipientIdentifier) {
        this.recipientIdentifier = recipientIdentifier;
    }

    public ParticipantIdentifierType getSenderIdentifier() {
        return senderIdentifier;
    }

    public void setSenderIdentifier(ParticipantIdentifierType senderIdentifier) {
        this.senderIdentifier = senderIdentifier;
    }

    public DocumentIdentifierType getDocumentIdentifier() {
        return documentIdentifier;
    }

    public void setDocumentIdentifier(DocumentIdentifierType documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    }

    public ProcessIdentifierType getProcessIdentifier() {
        return processIdentifier;
    }

    public void setProcessIdentifier(ProcessIdentifierType processIdentifier) {
        this.processIdentifier = processIdentifier;
    }

    public String getRecipient() {
        return recipientIdentifier.getValue();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SoapHeader");
        sb.append("{messageIdentifier='").append(messageIdentifier).append('\'');
        sb.append(", channelIdentifier='").append(channelIdentifier).append('\'');
        sb.append(", recipientIdentifier=").append("{scheme=").append(recipientIdentifier.getScheme()).append(",value=").append(recipientIdentifier.getValue()).append("}");
        sb.append(", senderIdentifier=").append("{scheme=").append(senderIdentifier.getScheme()).append(",value=").append(senderIdentifier.getValue()).append("}");
        sb.append(", documentIdentifier=").append(documentIdentifier.getValue());
        sb.append(", processIdentifier=").append(processIdentifier.getValue());
        sb.append('}');
        return sb.toString();
    }
}
