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

package network.oxalis.persistence.aop;

import com.google.inject.Inject;
import network.oxalis.persistence.annotation.Transactional;
import network.oxalis.persistence.api.JdbcTxManager;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Looks for all @Repository annotations and makes a database connection available to the
 * methods within the repository.
 * <p>
 * User: andy
 * Date: 8/13/12
 * Time: 2:19 PM
 */
public class RepositoryConnectionMethodInterceptor implements MethodInterceptor {

    @Inject
    private JdbcTxManager jdbcTxManager;

    /**
     * Starts a jdbc transaction if a transaction doesnt already exist.
     * Joins the transaction if one exists
     *
     * @param invocation the method invocation joinpoint
     * @return the result of the call to {@link
     * org.aopalliance.intercept.Joinpoint#proceed()}, might be intercepted by the
     * interceptor.
     * @throws Throwable if the interceptors or the
     *                   target-object throws an exception.
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        //Ignore the call if the current method is annotated with Transactional
        if (invocation.getMethod().isAnnotationPresent(Transactional.class)) {
            jdbcTxManager.trace(String.format(
                    "Ignoring method %s because it is annotated with Transactional",
                    invocation.getMethod().toGenericString()));
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
        } finally {
            //Essential that we clean up
            jdbcTxManager.cleanUp();
        }
    }
}
