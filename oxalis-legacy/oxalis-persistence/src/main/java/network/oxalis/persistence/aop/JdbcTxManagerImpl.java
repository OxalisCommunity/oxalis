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
import lombok.extern.slf4j.Slf4j;
import network.oxalis.persistence.api.JdbcTxManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implementation of a transaction manager, which is responsible
 * for handling a Connection object which is placed into ThreadLocal.
 * <p>
 * It is responsible for fetching Connection objects from a DataSource, and setting
 * them up so that they can be transactional (autoCommit --&gt; false).
 * <p>
 * It also can be used to rollback programatically an existing transaction.
 */
@Slf4j
public class JdbcTxManagerImpl implements JdbcTxManager {

    private static int instances = 0;

    /**
     * Used to track problems with multiple instances being created.
     */
    private int id;

    /**
     * Stores a thread local copy of the current connection
     */
    private final ThreadLocal<JdbcTransaction> threadLocalJdbcTransaction = new ThreadLocal<>();

    private final DataSource dataSource;

    @Inject
    public JdbcTxManagerImpl(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource not supplied in constructor");
        }
        this.id = instances;
        instances++;
        trace("new instance");
        this.dataSource = dataSource;
    }

    @Override
    public boolean isTransaction() {

        try {
            final Connection connection = getThreadLocalConnection();
            return connection != null && !connection.getAutoCommit();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to check if a transaction has been started", e);
        }
    }

    @Override
    public boolean isConnection() {
        final Connection connection = getThreadLocalConnection();
        return connection != null;
    }

    @Override
    public void newConnection(boolean autoCommit) {

        try {
            //only allowed to create a new transaction if the old one is commited.
            if (isTransaction()) {
                final String message = "Unable to start a new transaction existing connection is not commited";
                trace(message);
                throw new IllegalStateException(message);
            }

            //fetches the connection from the datasource.
            final Connection connection = dataSource.getConnection();

            //sets whether or not the connection should autocommit.
            connection.setAutoCommit(autoCommit);

            //adds the connection to the current thread
            final JdbcTransaction jdbcTransaction = new JdbcTransaction(connection);
            threadLocalJdbcTransaction.set(jdbcTransaction);

        } catch (SQLException e) {
            final String message = "Unable to get a connection from the provided datasource";
            trace(message);
            throw new IllegalStateException(message, e);
        }
    }

    @Override
    public void commit() {

        try {
            if (!isTransaction()) {
                final String message = "Unable to commit transaction connection, no transaction exists";
                trace(message);
                throw new IllegalStateException(message);
            }

            final JdbcTransaction jdbcTransaction = threadLocalJdbcTransaction.get();
            //if the transaction has been marked for rollback, rollback the transaction.
            if (jdbcTransaction.isRollback()) {
                trace("Not commiting - Transaction marked for rollback");
                rollback();
            } else {
                //Commits the transaction... connection cannot be null as the isTransaction method tests for that
                trace("Commiting transaction");
                jdbcTransaction.getConnection().commit();
            }

        } catch (SQLException e) {
            final String message = "Unable to commit the transaction";
            trace(message);
            throw new IllegalStateException(message, e);
        }
    }

    @Override
    public void rollback() {
        try {
            if (!isTransaction()) {
                final String message = "Unable to rollback transaction, no transaction exists";
                trace(message);
                throw new IllegalStateException(message);
            }
            getThreadLocalConnection().rollback();
        } catch (SQLException e) {
            final String message = "Unable to rollback the transaction";
            trace(message);
            throw new IllegalStateException(message, e);
        }
    }

    @Override
    public void cleanUp() {
        try {
            //closes the connection
            final Connection connection = getThreadLocalConnection();
            if (connection != null) {
                trace("closing connection");
                connection.close();
            }
        } catch (SQLException e) {
            final String message = "Unable to close the connection";
            trace(message);
            throw new IllegalStateException(message, e);
        } finally {
            //Essential that we remove the reference to thread local to avoid memory leaks
            trace("Removing transaction manager");
            threadLocalJdbcTransaction.set(null);
            threadLocalJdbcTransaction.remove();    // Ensures we don't get memory leaks
        }
    }

    /**
     * Gets the connection
     *
     * @return
     */
    @Override
    public Connection getConnection() {
        final Connection connection = getThreadLocalConnection();
        if (connection == null) {
            final String message = "Unable to get the connection. Did you forget to annotate the method with @Transactional or the repository with @Repository?";
            trace(message);
            throw new IllegalStateException(message);
        }
        try {
            if (connection.isClosed()) {
                throw new IllegalStateException("Connection is closed!");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to inspect connection: " + e.getMessage(), e);
        }
        return connection;
    }

    /**
     * marks the transaction to be rollbacked
     */
    @Override
    public void setRollbackOnly() {
        final JdbcTransaction jdbcTransaction = threadLocalJdbcTransaction.get();
        if (jdbcTransaction == null) {
            final String message = "Unable to mark the transaction as rollbackOnly. Did you forget to annotate the method with @Transactional or the repository with @Repository?";
            trace(message);
            throw new IllegalStateException(message);
        }
        trace("Transaction marked for rollback");
        jdbcTransaction.setRollback(true);
    }

    /**
     * Helper method for null safe fetching of the JDBC Connection.
     *
     * @return
     */
    private Connection getThreadLocalConnection() {
        final JdbcTransaction jdbcTransaction = threadLocalJdbcTransaction.get();
        return jdbcTransaction == null ? null : jdbcTransaction.getConnection();
    }

    /**
     * logs a debug message with the current transaction object
     *
     * @param message
     */
    @Override
    public void trace(String message) {
        if (log.isDebugEnabled()) {
            JdbcTransaction jdbcTransaction = threadLocalJdbcTransaction.get();
            final String transaction = jdbcTransaction == null ? "" : ("" + jdbcTransaction.hashCode());
            log.debug(String.format("Trace %s:%s\t>>\t%s", id, transaction, message));
        }
    }

    /**
     * Helper class that holds a Connection object and whether or not the transaction should be rolled back.
     */
    private class JdbcTransaction {

        private final Connection connection;

        private boolean rollback = false;

        private JdbcTransaction(Connection connection) {
            this.connection = connection;
        }

        public Connection getConnection() {
            return connection;
        }

        public void setRollback(boolean rollback) {
            this.rollback = rollback;
        }

        public boolean isRollback() {
            return rollback;
        }
    }
}
