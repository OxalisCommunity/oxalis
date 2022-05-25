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

package network.oxalis.as2.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author erlend
 */
public class MessageIdUtilTest {

    @Test
    public void simple() {
        Assert.assertTrue(MessageIdUtil.verify("<1060501332.515.1528302064500@de77cf5d0088>"));
        Assert.assertTrue(MessageIdUtil.verify("<OpenPEPPOL-06062018160101+0300-0447@APP_1000000200_APP_2000000300>"));

        Assert.assertFalse(MessageIdUtil.verify("<1060501332..515.1528302064500@de77cf5d0088>"));
        Assert.assertFalse(MessageIdUtil.verify(" <1060501332.515.1528302064500@de77cf5d0088>"));
        Assert.assertFalse(MessageIdUtil.verify("<1060501332.515.1528302064500@de77cf5d0088"));
        Assert.assertFalse(MessageIdUtil.verify("<1060501332.515.1528302064500_de77cf5d0088>"));

        Assert.assertTrue(MessageIdUtil.verify("<Aa1!#$%&'*+-/=?^_`{|}~.Aa1!#$%&'*+-/=?^_`{|}~@Aa1!#$%&'*+-/=?^_`{|}~.Aa1!#$%&'*+-/=?^_`{|}~>"));
    }

}
