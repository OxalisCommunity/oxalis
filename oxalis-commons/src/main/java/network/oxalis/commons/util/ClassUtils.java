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

package network.oxalis.commons.util;

import network.oxalis.api.lang.OxalisLoadingException;

/**
 * @author erlend
 */
public class ClassUtils {

    /**
     * Loads a class from current class loader.
     */
    public static Class<?> load(String className) {
        return load(className, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Loads a class from the given class loader.
     */
    public static Class<?> load(String className, ClassLoader classLoader) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new OxalisLoadingException(String.format("Unable to load class '%s'.", classLoader), e);
        }
    }
}
