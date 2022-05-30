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

package network.oxalis.commons.filesystem;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

/**
 * Class holding some utils related to file handling.
 *
 * @author erlend
 * @since 4.0.0
 */
public class FileUtils {

    /**
     * Filter string to make it better fit use as filename.
     *
     * @param s Unfiltered string.
     * @return Filtered string.
     */
    public static String filterString(String s) {
        return s.replaceAll("[^a-zA-Z0-9.\\-]", "_");
    }

    public static URL toUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
