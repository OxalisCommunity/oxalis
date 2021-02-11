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

import network.oxalis.api.util.Sort;

import java.util.Comparator;

/**
 * Class containing methods to make Sortable interface and Sort
 * annotation useful.
 *
 * @author erlend
 */
public class Sortables {

    /**
     * Method extracting order value from a given object.
     *
     * @param <T> The type of the elements to be sorted.
     * @param o   Object to extract value for comparison.
     * @return Value used to compare.
     */
    public static <T> int extract(T o) {
        if (o.getClass().isAnnotationPresent(Sort.class))
            return o.getClass().getAnnotation(Sort.class).value();

        return 0;
    }

    /**
     * Returns a comparator.
     *
     * @param <T> The type of the elements to be sorted.
     * @return Comparator to be used for sorting.
     */
    public static <T> Comparator<T> comparator() {
        return Comparator.comparingInt(Sortables::extract);
    }
}