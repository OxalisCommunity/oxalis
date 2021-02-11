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

package network.oxalis.persistence.api;

import java.sql.Connection;

/**
 * Manages JDBC transactions.
 *
 * Provides access to the connection object which is automagically fetched
 * as long as the the method is in a class annotated with @Repository or
 * the method is annotated with @Transactional
 * The connection is stored in ThreadLocal
 *
 * User: andy
 * Date: 8/9/12
 * Time: 1:23 PM
 */
public interface JdbcTxManager {

    /**
     * Checks whether the current connection is not null and autocomit is set to false.
     * @return
     */
    boolean isTransaction();

    /**
     * Checks if the current connection is null.
     * @return
     */
    boolean isConnection();

    /**
     * N.B. DO NOT USE THIS METHOD DIRECTLY. It is only to be used by the TransactionalMethodInterceptor
     * and the RepositoryConnectionMethodInterceptor
     *
     * Makes a Connection object available from the getConnection() method with either autocomit set to true or false.
     *
     * @param autoCommit automatically commits after each execute. Set to false to use transactions.
     */
    void newConnection(boolean autoCommit);

    /**
     * Commits the current connection
     * Requires that the existing connection was created with autocommit set to false.
     */
    void commit();

    /**
     * Rollbacks the current connection
     */
    void rollback();

    /**
     * Closes the connection and removes the connection from the thread local
     */
    void cleanUp();

    /**
     * Gets the connection that is currently being used.
     * @return
     */
    Connection getConnection();

    /**
     * Marks the transaction for rollback when completing.
     * The connection will always be rolled back even when an exception does not occur.
     */
    void setRollbackOnly();


    void trace(String message);
}
