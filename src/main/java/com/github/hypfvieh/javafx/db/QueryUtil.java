package com.github.hypfvieh.javafx.db;

import java.io.Closeable;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

/**
 * Utility to query the database and taking care of exceptions and transactions.
 * <p>
 * <strong>Requires hibernate, please add it to your project, otherwise using this class will fail
 * </strong>
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class QueryUtil implements Closeable {
    private final Logger logger;
    private Session defaultSession;

    private final SessionFactory dbFactory;

    public QueryUtil(SessionFactory _dbFactory) {
        logger = LoggerFactory.getLogger(getClass());
        dbFactory = _dbFactory;
        defaultSession = _dbFactory.openSession();
    }


    /**
     * Execute the given Consumer in the database session.
     * Optionally catch all exceptions which might be thrown.
     * <br><br>
     * Uses the default internal session to execute query.<br>
     * If internal session was closed, a new session will be created and used as new default session
     * <br>
     * @param _toExecute consumer to execute
     * @param _catchAllExceptions true to catch all exception, false to re-throw
     */
    public void executeInSession(Consumer<Session> _toExecute, boolean _catchAllExceptions) {
        executeSession(defaultSession, _toExecute, _catchAllExceptions);
    }

    /**
     * Execute the given Consumer in the database session.
     * Re-throws all exceptions.
     *
     * <br><br>
     * Uses the default internal session to execute query.<br>
     * If internal session was closed, a new session will be created and used as new default session
     * <br>

     * @param _toExecute consumer to execute
     */
    public void executeInSession(Consumer<Session> _toExecute) {
        executeInSession(_toExecute, false);
    }


    /**
     * Execute the given Consumer in the database session.
     * Catches all exceptions.
     * <br><br>
     * Uses the default internal session to execute query.<br>
     * If internal session was closed, a new session will be created and used as new default session
     * <br>
     *
     * @param _toExecute consumer to execute
     */
    public void executeInSessionCatchAll(Consumer<Session> _toExecute) {
        executeInSession(_toExecute, true);
    }


    /**
     * Execute the given Consumer in the database session.
     * Optionally catch all exceptions which might be thrown.
     * <br><br>
     * Creates a new session, executes the query and closes the session after query.
     * <br>
     * @param _toExecute consumer to execute
     * @param _catchAllExceptions true to catch all exception, false to re-throw
     */
    public void executeInNewSession(Consumer<Session> _toExecute, boolean _catchAllExceptions) {
        executeSession(null, _toExecute, _catchAllExceptions);
    }

    /**
     * Execute the given Consumer in the database session.
     * Re-throws all exceptions.
     *
     * <br><br>
     * Creates a new session, executes the query and closes the session after query.
     * <br>
     * @param _toExecute consumer to execute
     */
    public void executeInNewSession(Consumer<Session> _toExecute) {
        executeSession(null, _toExecute, false);
    }

    /**
     * Execute the given Consumer in the database session.
     * Catches all exceptions.
     * <br><br>
     * Uses the default internal session to execute query.<br>
     * If internal session was closed, a new session will be created and used as new default session
     * <br>
     *
     * @param _toExecute consumer to execute
     */
    public void executeInNewSessionCatchAll(Consumer<Session> _toExecute) {
        executeInNewSession(_toExecute, true);
    }

    /**
     * Execute the given Consumer in the database session.
     * Optionally catch all exceptions which might be thrown.
     * <br><br>
     * To execute the query in a new session automatically, use null as _session parameter.
     * The new session will automatically closed after query.<br>
     * If _session is non-null and
     *
     * @param _session session on which the query is executed, if null new session is used
     * @param _toExecute consumer to execute
     * @param _catchAllExceptions true to catch all exception, false to re-throw
     */
    public void executeSession(Session _session, Consumer<Session> _toExecute, boolean _catchAllExceptions) {
        Session session = getOrCreateSession(_session);
        try {
            // reset any transaction which may be pending on this session
            fixOpenTransactions(session);
            _toExecute.accept(session);
            if (_session == null && session.isConnected()) {
                session.close();
            }

        } catch (RuntimeException _ex) {
            logger.error("Error while performing database action.", _ex);
            fixOpenTransactions(session);
            if (!_catchAllExceptions) {
                throw _ex;
            }
        }
    }

    /**
     * Execute given function in default session wrapped in a transaction.
     * If function throws a {@link RuntimeException}, transaction is rolled back.
     * @param _func consumer to execute (will receive current session)
     */
    public void doInTransaction(Consumer<Session> _func) {
        if (_func == null) {
            return;
        }
        executeInSession(session -> {
            doTransaction(_func, session);
        });
    }

    /**
     * Execute given function in a new session wrapped in a transaction.
     * If function throws a {@link RuntimeException}, transaction is rolled back.
     * @param _func consumer to execute (will receive current session)
     */
    public void doInTransactionInNewSession(Consumer<Session> _func) {
        if (_func == null) {
            return;
        }
        executeInNewSession(session -> {
            doTransaction(_func, session);
        });
    }

    /**
     * Execute function on the given session.
     * 
     * @param _func function
     * @param _session session
     */
    private void doTransaction(Consumer<Session> _func, Session _session) {
        _session.beginTransaction();
        try {
            _func.accept(_session);
            _session.getTransaction().commit();
        } catch (RuntimeException _ex) {
            _session.getTransaction().rollback();
            throw _ex;
        }
    }
    
    /**
     * Execute the given Function in the database session.
     * Optionally catch all exceptions which might be thrown.
     *
     * @param _toExecute consumer to execute
     * @param _catchAllExceptions true to catch all exception, false to re-throw
     *
     * @return Result of whatever the function should return
     * @param <T> type of action result
     */
    public <T> T queryInSession(Function<Session, T> _toExecute, boolean _catchAllExceptions) {
        return querySession(defaultSession, _toExecute, _catchAllExceptions);
    }

    /**
     * Execute the given Function in the database session.
     * Catches all exceptions.
     *
     * @param _toExecute consumer to execute
     *
     * @return Result of whatever the function should return
     * @param <T> type of action result
     */
    public <T> T queryInSessionCatchAll(Function<Session, T> _toExecute) {
        return queryInSession(_toExecute, true);
    }

    /**
     * Execute the given Function in the database session.
     * Re-throws all exceptions.
     *
     * @param _toExecute consumer to execute
     *
     * @return Result of whatever the function should return
     * @param <T> type of action result
     */
    public <T> T queryInSession(Function<Session, T> _toExecute) {
        return queryInSession(_toExecute, false);
    }

    /**
     * Execute the given Function in the database session.
     * Optionally catches or re-throws all exceptions.
     * <br><br>
     * Creates a new session, executes the query and closes the session after query.
     * <br>
     * @param _toExecute consumer to execute
     * @param _catchAllExceptions true to catch all exceptions, false to re-throw
     *
     * @return Result of whatever the function should return
     * @param <T> type of action result
     */
    public <T> T queryInNewSession(Function<Session, T> _toExecute, boolean _catchAllExceptions) {
        return querySession(null, _toExecute, _catchAllExceptions);
    }

    /**
     * Execute the given Function in the database session.
     * Re-throws all exceptions.
     * <br><br>
     * Creates a new session, executes the query and closes the session after query.
     * <br>
     * @param _toExecute consumer to execute
     *
     * @return Result of whatever the function should return
     * @param <T> type of action result
     */
    public <T> T queryInNewSession(Function<Session, T> _toExecute) {
        return queryInNewSession(_toExecute, false);
    }

    /**
     * Execute the given Function in the database session.
     * Catches all exceptions.
     * <br><br>
     * Creates a new session, executes the query and closes the session after query.
     * <br>
     * @param _toExecute consumer to execute
     *
     * @return Result of whatever the function should return
     * @param <T> type of action result
     */
    public <T> T queryInNewSessionCatchAll(Function<Session, T> _toExecute) {
        return queryInNewSession(_toExecute, true);
    }

    /**
     * Execute the given Function in the given database session.
     *
     * @param _session session to execute function with
     * @param _toExecute function to execute
     * @param _catchAllExceptions true to catch all exceptions, false to re-throw
     *
     * @return Result of whatever the function should return
     * @param <T> type of action result
     */
    public <T> T querySession(Session _session, Function<Session, T> _toExecute, boolean _catchAllExceptions) {
        Session session = getOrCreateSession(_session);

        try {
            fixOpenTransactions(session);
            T result = _toExecute.apply(session);
            if (_session == null && session.isConnected()) {
                session.close();
            }
            return result;
        } catch (RuntimeException _ex) {
            logger.error("Error while performing database action.", _ex);
            fixOpenTransactions(session);
            if (!_catchAllExceptions) {
                throw _ex;
            }
            return null;
        }
    }


    /**
     * Close the underlying session.
     * All subsequent calls will then fail.
     */
    public void closeSession() {
        if (defaultSession.isOpen()) {
            logger.info("Closing DB session {}", defaultSession);
            defaultSession.close();
        }
    }

    /**
     * Close open session and factory.
     */
    @Override
    public void close() {
        closeSession();
        dbFactory.close();
    }

    public boolean isClosed() {
        return dbFactory == null || dbFactory.isClosed();
    }

    /**
     * Check if session is valid (open) and reset all transactions which might be pending.
     */
    private void fixOpenTransactions(Session _session) {
        if (!_session.isOpen()) {
            throw new IllegalStateException("Database session already closed");
        }
        if (_session.getTransaction().isActive()) {
            _session.getTransaction().rollback();
        }
    }


    /**
     * Checks the given session.
     * If session was closed a new session will be created.<br>
     * If null is given, a new session will be created.<br>
     * If the given session is still valid, this session will be returned.<br>
     * If the given session is not null and is closed and is equal to the internal default session,
     * a new session will created and configured as new default session.
     *
     * @param _session session to check
     * @return given session or new session, never null
     */
    private Session getOrCreateSession(Session _session) {
        Session session;
        if (_session == null) {
            session = dbFactory.openSession();
        } else {
            if (!_session.isOpen()) { // session was closed before, create new session
                session = dbFactory.openSession();

                // the given session was the default session and it was closed
                // update the default session to the new session (old closed session cannot be used anymore)
                if (defaultSession == _session) {
                    defaultSession = session;
                }
            } else {
                session = _session;
            }
        }
        return session;
    }

    /**
     * Execute the given query and will return the result or null on failure or no result.
     *
     * @param <T> type
     * @param _query query to execute
     *
     * @return instance of type or null
     */
    public static <T> T getResultOrNull(TypedQuery<T> _query) {
        return getResultOrDefault(_query, null);
    }

    /**
     * Execute the given query and return the result or the default on failure or no result.
     *
     * @param <T> type
     * @param _query query to execute
     * @param _default default to provide if no result
     * @return instance of type or default value of same type
     */
    public static <T> T getResultOrDefault(TypedQuery<T> _query, T _default) {
        try {
            T singleResult = _query.getSingleResult();

            return singleResult == null && _default != null ? _default : singleResult;
        } catch (NoResultException _ex) {
            return _default;
        }
    }

}
