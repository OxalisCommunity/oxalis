/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.peppol.as2.inbound;

import eu.peppol.as2.model.MdnData;

import javax.mail.internet.MimeMessage;

/**
 * Holds the data to be returned back to the servlet, which will use this to create a http response.
 *
 * @author steinar
 */
class ResponseData {

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
