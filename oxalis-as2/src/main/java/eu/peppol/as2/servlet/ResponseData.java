package eu.peppol.as2.servlet;

import eu.peppol.as2.MdnData;

import javax.mail.internet.MimeMessage;

/**
 * Holds the data to be returned back to the servlet, which will use this to create a http response.
 * <p>
 * Created by soc on 18.01.2016.
 */
public class ResponseData {

    private MimeMessage signedMdn;
    private final MdnData mdnData;
    private int httpStatus;

    public ResponseData(int status, MimeMessage signedMdn, MdnData mdnData) {
        httpStatus = status;
        this.signedMdn = signedMdn;
        this.mdnData = mdnData;
    }

    /**
     * The signed MDN
     */
    public MimeMessage getSignedMdn() {
        return signedMdn;
    }

    /**
     * The http status code to be returned
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    public MdnData getMdnData() {
        return mdnData;
    }
}
