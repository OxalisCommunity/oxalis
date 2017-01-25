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

package eu.peppol.as2.lang;

/**
 * Indicates that the MDN request could not be handled. I.e. the requested protocol is not available or there
 * was an error during the parsing of the header "disposition-notification-options"
 *
 * @author steinar
 *         Date: 17.10.13
 *         Time: 22:27
 */
public class MdnRequestException extends OxalisAs2Exception {

    public MdnRequestException(String msg) {
        super(msg);
    }
}
