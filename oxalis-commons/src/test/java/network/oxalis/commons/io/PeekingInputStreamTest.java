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

package network.oxalis.commons.io;

import com.google.common.io.ByteStreams;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class PeekingInputStreamTest {

    @Test
    public void simple() throws IOException {
        PeekingInputStream peekingInputStream = new PeekingInputStream(
                new ByteArrayInputStream("Hello World!".getBytes()));

        byte[] bytes1 = new byte[5];
        peekingInputStream.read(bytes1);
        Assert.assertEquals(new String(bytes1), "Hello");

        Assert.assertEquals(new String(ByteStreams.toByteArray(peekingInputStream.newInputStream())), "Hello World!");
    }
}

