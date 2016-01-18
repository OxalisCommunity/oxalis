package eu.peppol.as2.servlet;

import javax.mail.internet.InternetHeaders;

/**
 * Holds the data to be returned back to the servlet, which will use this to create a http response.
 *
 * Created by soc on 18.01.2016.
 */
public class ResponseData {

    int httpStatus;
    InternetHeaders addHeaders;
    byte[] responseData;

    public ResponseData(byte[] responseData) {
        this.responseData = responseData;

        addHeaders = new InternetHeaders();
    }
}
