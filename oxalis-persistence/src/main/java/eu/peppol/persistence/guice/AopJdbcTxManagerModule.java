package eu.peppol.persistence.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;
import eu.peppol.persistence.guice.jdbc.*;

/**
* @author andy
* @author thore
 *
*/
public class AopJdbcTxManagerModule extends AbstractModule {

    @Override
    protected void configure() {

        // The tx Manager needs to be a singleton so that the same reference to the threadlocal containing the connection is shared amongst repositories etc.
        bind(JdbcTxManagerImpl.class).in(Scopes.SINGLETON);
        bind(JdbcTxManager.class).to(JdbcTxManagerImpl.class);

        //creates the transactional method interceptor which handles methods annotated with @Transactional
        final TransactionalMethodInterceptor transactionalMethodInterceptor = new TransactionalMethodInterceptor();

        // Creates the  method interceptor which handles methods annotated with @Repository
        final RepositoryConnectionMethodInterceptor repositoryConnectionMethodInterceptor = new RepositoryConnectionMethodInterceptor();

        //injects the JdbcTxManager into the method interceptor for methods annotated with @Transactional.
        requestInjection(transactionalMethodInterceptor);

        //injects the JdbcTxManager into the method interceptor for methods annotated with @Repository
        requestInjection(repositoryConnectionMethodInterceptor);

        //makes individual methods in a class transactional.
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), transactionalMethodInterceptor);

        //makes all methods in the class transactional
        bindInterceptor(Matchers.annotatedWith(Transactional.class), Matchers.any(), transactionalMethodInterceptor);
        bindInterceptor(Matchers.annotatedWith(Repository.class), Matchers.any(), repositoryConnectionMethodInterceptor);

    }

}
