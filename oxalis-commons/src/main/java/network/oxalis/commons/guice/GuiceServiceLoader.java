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

package network.oxalis.commons.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import network.oxalis.api.lang.OxalisPluginException;
import network.oxalis.commons.util.ClassUtils;

import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class works by the same principles as {@link java.util.ServiceLoader}, however this implementation loads each
 * detected class using the Guice injector.
 *
 * @author erlend
 */
public class GuiceServiceLoader {

    private static final String PREFIX = "META-INF/services/";

    private final Injector injector;

    @Inject
    public GuiceServiceLoader(Injector injector) {
        this.injector = injector;
    }

    public <T> List<T> load(Class<T> cls, ClassLoader classLoader) {
        try {
            // Find all instances of files in class loader.
            return Collections.list(classLoader.getResources(PREFIX + cls.getName())).stream()
                    // Fetch all lines in all detected files.
                    .map(this::getLines)
                    // Convert stream of lists to stream.
                    .flatMap(Collection::stream)
                    // Load classes referenced in detected files.
                    .map(s -> (Class<T>) ClassUtils.load(s, classLoader))
                    // Load each class using Guice magic.
                    .map(injector::getInstance)
                    // Collect all instances to a list.
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new OxalisPluginException("Unable to load resources.", e);
        }
    }

    /**
     * Extracts all lines in the provided target.
     */
    private List<String> getLines(URL url) {
        try (InputStream inputStream = url.openStream();
             Reader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            // Read all lines.
            return bufferedReader.lines()
                    // Trim all lines.
                    .map(String::trim)
                    // Collect all lines.
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new OxalisPluginException("Unable to read.", e);
        }
    }
}
