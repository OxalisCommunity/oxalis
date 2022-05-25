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
import network.oxalis.persistence.api.JdbcTxManager;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Looks for all @Transactional annotations and injects code for starting and stopping transactions.
 * <p>
 * User: andy
 * Date: 8/13/12
 * Time: 2:19 PM
 */
public class TransactionalMethodInterceptor implements MethodInterceptor {

    @Inject
    private JdbcTxManager jdbcTxManager;

    /**
     * Starts a jdbc transaction if a transaction doesnt already exist.
     * Joins the transaction if one exists.
     *
     * @param invocation the method invocation joinpoint
     * @return the result of the call to {@link
     * org.aopalliance.intercept.Joinpoint#proceed()}, might be intercepted by the
     * interceptor.
     * @throws Throwable if the interceptors or the
     *                   target-object throws an exception.
     *                   IllegalStateException if there already exists a connection which is not transactional
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        //we need to find out whether or not there is an existing transaction or an existing Connection
        final boolean transaction = jdbcTxManager.isTransaction();
        final boolean connection = jdbcTxManager.isConnection();

        //If there is a transaction running do nothing as the transaction will be cleaned up by the
        //code which created the transaction
        if (transaction) {
            //tx already exists so continues operation.
            jdbcTxManager.trace(String.format("Transaction already exists so not starting a new one when calling method: %s", invocation.getMethod().getName()));
            return invocation.proceed();
        }

        //If there is a connection we have decided that this is an error because it means that
        //a non transactional method in a repository is calling a transactional method elsewhere.
        //which we believe is BAD DESIGN. (It would be possible to implement using a separate variable
        //for the transactional connection if we ever change our minds ;))
        if (connection) {
            throw new IllegalStateException("Unable to start a transaction, there already exists a connection which is not transactional" + invocation.getMethod().getName());
        }

        try {
            // Starts the transaction by setting the autocommit value to be false on the connection.
            jdbcTxManager.newConnection(false);
            jdbcTxManager.trace("Started new transaction due to annotation on method: " + invocation.getMethod().getName());
            //makes the call to the method that is being wrapped.
            Object returnValue = invocation.proceed();

            // Tries to commit the transaction.
            // it is still possible that the TxManager will rollback the transaction,
            // but as far as we are concerned our code worked as expected
            jdbcTxManager.commit();

            //returns the result of the wrapped method call
            return returnValue;
        } catch (Throwable thr) {
            //if an exception is thrown we need to rollback the transaction.
            jdbcTxManager.trace("Rolling back transaction due to exception: " + thr.getMessage());
            jdbcTxManager.rollback();
            jdbcTxManager.trace("Rolling back transaction ok");
            //rethrows the exception so that it can be handled by the calling code
            throw thr;
        } finally {
            //Essential that we clean up as we are placing connections on thread local
            jdbcTxManager.cleanUp();
        }
    }
}
