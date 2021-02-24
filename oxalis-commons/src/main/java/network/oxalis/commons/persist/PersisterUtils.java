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

package network.oxalis.commons.persist;

import network.oxalis.commons.filesystem.FileUtils;
import network.oxalis.vefa.peppol.common.model.Header;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author erlend
 * @since 4.0.0
 */
public class PersisterUtils {

    /**
     * Computes the Path for a directory into which your file artifacts associated with
     * the supplied header may be written. Any intermediate directories are created for you.
     *
     * @param baseFolder the root folder to use as the basis for appending additional folders.
     * @param header     meta data to be used as input for computation.
     * @return a path to a directory into which you may store your artifacts.
     * @throws IOException
     */
    public static Path createArtifactFolders(Path baseFolder, Header header) throws IOException {
        Path folder = baseFolder.resolve(Paths.get(
                FileUtils.filterString(header.getReceiver().getIdentifier()),
                FileUtils.filterString(header.getSender().getIdentifier())));

        Files.createDirectories(folder);

        return folder;
    }
}
