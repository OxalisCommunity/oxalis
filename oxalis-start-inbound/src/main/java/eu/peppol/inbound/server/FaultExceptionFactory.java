/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.inbound.server;

import eu.peppol.start.StartSubCode;
import org.w3._2009._02.ws_tra.FaultMessage;
import org.w3._2009._02.ws_tra.StartException;

/**
 * @author steinar
 *         Date: 08.06.13
 *         Time: 11:35
 */
public class FaultExceptionFactory {


    public static FaultMessage createServerException(String message, Exception e) {

        StartException startException = createStartException(StartSubCode.SERVER_ERROR, e.getMessage());

        FaultMessage faultMessage = new FaultMessage(message, startException, e);

        return faultMessage;
    }

    static StartException createStartException(StartSubCode startSubCode, String details) {
        StartException startException = new StartException();
        startException.setAction("http://busdox.org/2010/02/channel/fault");
        startException.setFaultcode("s:Sender");
        startException.setFaultstring(startSubCode.toString());
        startException.setDetails(details);
        return startException;
    }

    public static FaultMessage createServerException(String message) {
        StartException startException = createStartException(StartSubCode.SERVER_ERROR, message);
        FaultMessage faultMessage = new FaultMessage(message, startException);
        return faultMessage;
    }
}
