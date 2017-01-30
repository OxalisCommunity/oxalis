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

package no.difi.oxalis.commons.io;

import java.io.*;

/**
 * Caching InputStream to be used when reading the beginning of a stream is needed before the stream is "reset" when
 * the exact amount of data is unknown and support for marking of is irrelevant.
 *
 * @author erlend
 * @since 4.0.0
 */
public class PeekingInputStream extends InputStream {

    private final InputStream sourceInputStream;

    private final ByteArrayOutputStream cacheOutputStream = new ByteArrayOutputStream();

    public PeekingInputStream(InputStream sourceInputStream) {
        this.sourceInputStream = sourceInputStream;
    }

    @Override
    public int read() throws IOException {
        // Read byte
        int b = sourceInputStream.read();

        // Write to internal stream
        cacheOutputStream.write(b);

        // Return byte
        return b;
    }

    public InputStream newInputStream() throws IOException {
        return new SequenceInputStream(
                new ByteArrayInputStream(cacheOutputStream.toByteArray()),
                sourceInputStream
        );
    }
}
