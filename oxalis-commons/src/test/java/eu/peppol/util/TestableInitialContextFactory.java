/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Define a fake JNDI context factory, holding a scaled down Context with just enough power to make the test cases work.
 *
 * @author thore
 */
public class TestableInitialContextFactory implements InitialContextFactory {

    private static Context context;

    static {
        try {
            context = new InitialContext(true) {
                Map<String, Object> bindings = new HashMap<String, Object>();

                @Override
                public void bind(String name, Object obj) throws NamingException {
                    bindings.put(name, obj);
                }

                @Override
                public Object lookup(String name) throws NamingException {
                    return bindings.get(name);
                }

                @Override
                public void unbind(String name)throws NamingException {
                    bindings.remove(name);
                }

            };
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        return context;
    }

}
