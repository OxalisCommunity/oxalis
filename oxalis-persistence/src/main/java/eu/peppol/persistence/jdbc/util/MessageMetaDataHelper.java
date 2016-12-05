package eu.peppol.persistence.jdbc.util;

import eu.peppol.PeppolTransmissionMetaData;
import eu.peppol.persistence.ChannelProtocol;
import eu.peppol.persistence.MessageMetaData;
import eu.peppol.persistence.TransferDirection;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author steinar
 *         Date: 24.10.2016
 *         Time: 17.14
 */
public class MessageMetaDataHelper {

    /**
     * Converts an instance of {@link PeppolTransmissionMetaData} into a {@link MessageMetaData} object.
     *
     * The direction of the message transfer, typically always {@link eu.peppol.persistence.TransferDirection#IN} when you receive from the PEPPOL network,
     * hence this is the default.
     *
     * @param pm the {@link PeppolTransmissionMetaData} instance
     * @return instance of {@link MessageMetaData} with direction set to {@link TransferDirection#IN}
     */
    public static MessageMetaData createMessageMetaDataFrom(PeppolTransmissionMetaData pm) {

        MessageMetaData.Builder builder = new MessageMetaData.Builder(TransferDirection.IN, pm.getSenderId(), pm.getRecipientId(), pm.getDocumentTypeIdentifier(), ChannelProtocol.valueOf(pm.getProtocol().name()));

        builder .received(LocalDateTime.ofInstant(pm.getReceivedTimeStamp().toInstant(), ZoneId.systemDefault()))
                .delivered(pm.getSendersTimeStamp() != null ? LocalDateTime.ofInstant(pm.getSendersTimeStamp().toInstant(), ZoneId.systemDefault()) : null)
                .messageId(pm.getMessageId())
                .processTypeId(pm.getProfileTypeIdentifier())
                .accessPointIdentifier(pm.getSendingAccessPoint())
                .apPrincipal(pm.getSendingAccessPointPrincipal());

        MessageMetaData messageMetaData = builder.build();
        return messageMetaData;
    }


}
