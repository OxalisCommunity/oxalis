package eu.peppol.persistence.guice.jdbc;

import com.google.inject.Inject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Looks for all @Repository annotations and makes a database connection available to the
 * methods within the repository.
 *
 * User: andy
 * Date: 8/13/12
 * Time: 2:19 PM
 */
public class RepositoryConnectionMethodInterceptor implements MethodInterceptor {
    static final Logger log = LoggerFactory.getLogger(RepositoryConnectionMethodInterceptor.class);

    @Inject
    JdbcTxManager jdbcTxManager;

    /**
     * Starts a jdbc transaction if a transaction doesnt already exist.
     * Joins the transaction if one exists
     *
     * @param invocation the method invocation joinpoint
     * @return the result of the call to {@link
     *         org.aopalliance.intercept.Joinpoint#proceed()}, might be intercepted by the
     *         interceptor.
     * @throws Throwable if the interceptors or the
     *                   target-object throws an exception.
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        //Ignore the call if the current method is annotated with Transactional
        if (invocation.getMethod().isAnnotationPresent(Transactional.class)) {
            jdbcTxManager.trace(String.format("Ignoring method %s because it is annotated with Transactional",invocation.getMethod().toGenericString()));
            return invocation.proceed();
        }

        //if there is already a connection do nothing as the connection will be cleaned up elsewhere
        if (jdbcTxManager.isConnection()) {
            // just continue
            jdbcTxManager.trace("Using existing connection for method " + invocation.getMethod().getName());
            return invocation.proceed();
        }


        // Commits/rollbacks the transaction
        try {
            jdbcTxManager.newConnection(true);
            jdbcTxManager.trace("Fetched connection from datasource");

            return invocation.proceed();
        } catch (Throwable thr) {
            throw thr;
        } finally {
            //Essential that we clean up
            jdbcTxManager.cleanUp();
        }
    }
}
