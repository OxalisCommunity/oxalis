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
