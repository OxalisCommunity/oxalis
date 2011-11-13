package eu.peppol.inbound.transport;

import eu.peppol.outbound.soap.SoapHeader;
import org.w3c.dom.Document;

import java.io.IOException;

/**
 * Saves PEPPOL messages received into persistent storage.
 * 
 * @author Steinar Overbeck Cook (steinar@sendregning.no)
 * Date: 13.11.11
 * Time: 20:25
 */
public interface TransportChannelPersister {
    
    public void saveDocument(SoapHeader soapHeader, Document payloadDocument);
    
}
