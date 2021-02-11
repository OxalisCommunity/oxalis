/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package network.oxalis.as2.code;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author erlend
 */
public class DispositionTest {

    /**
     * RFC4130 7.5.6
     */
    @Test
    public void backeardCompatibility() {
        perform("automatic-action/MDN-sent-automatically; processed", DispositionType.PROCESSED, null, null);

        perform("automatic-action/MDN-sent-automatically; processed/error: authentication-failed",
                DispositionType.PROCESSED, DispositionModifier.ERROR,
                DispositionModifierExtension.AUTHENTICATION_FAILED);

        perform("automatic-action/MDN-sent-automatically;\n" +
                        "      processed/error: authentication-failed",
                DispositionType.PROCESSED, DispositionModifier.ERROR,
                DispositionModifierExtension.AUTHENTICATION_FAILED);

        perform("automatic-action/MDN-sent-automatically; processed/warning: duplicate-document",
                DispositionType.PROCESSED, DispositionModifier.WARNING,
                DispositionModifierExtension.DUPLICATE_DOCUMENT);

        perform("automatic-action/MDN-sent-automatically;\n" +
                        "      processed/warning: duplicate-document",
                DispositionType.PROCESSED, DispositionModifier.WARNING,
                DispositionModifierExtension.DUPLICATE_DOCUMENT);

        perform("automatic-action/MDN-sent-automatically; failed/failure: sender-equals-receiver",
                DispositionType.FAILED, DispositionModifier.FAILURE,
                DispositionModifierExtension.SENDER_EQUALS_RECEIVER);

        perform("automatic-action/MDN-sent-automatically;\n" +
                        "      failed/failure: sender-equals-receiver",
                DispositionType.FAILED, DispositionModifier.FAILURE,
                DispositionModifierExtension.SENDER_EQUALS_RECEIVER);
    }

    private void perform(String value, DispositionType type, DispositionModifier modifier,
                         DispositionModifierExtension extension) {
        Disposition disposition = Disposition.parse(value);

        Assert.assertEquals(disposition.getType(), type);
        Assert.assertEquals(disposition.getModifier(), modifier);
        Assert.assertEquals(disposition.getExtension(), extension);
        Assert.assertEquals(disposition.toString(), value.replaceAll("[ \r\n\t]+", " "));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void invalidValue() {
        Disposition.parse("Hello World");
    }
}
