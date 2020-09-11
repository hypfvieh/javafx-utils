package com.github.hypfvieh.javafx.other;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple class which will use a {@link ServerSocket} to prevent the application from being started more than once.
 * It uses the java user preference store to remember lock information.
 *
 * @author hypfvieh
 * @since v1.0.0 - 2019-07-04
 */
public final class AppLock implements AutoCloseable {

    private static final String APP_LOCK_MARKER = "#AppLock";

    private final Logger logger;
    private final Preferences userPrefs;

    private Class<?> mainClass;
    private ServerSocket serverSock;

    /**
     * Create a new AppLock instance using the given lock file, will throw if application is already running.
     * @param _mainClass main class to apply lock to (never null)
     * @throws AppAlreadyRunningException if application already running
     */
    public AppLock(Class<?> _mainClass) throws AppAlreadyRunningException {
        logger = LoggerFactory.getLogger(getClass());
        mainClass = Objects.requireNonNull(_mainClass, "Mainclass cannot be null");
        userPrefs = Preferences.userRoot().node(getClass().getName().replace('.', '/'));

        checkLock();
    }

    /**
     * Checks previous locks and locks if application not yet running.
     * @throws AppAlreadyRunningException if application is already running
     */
    private void checkLock() throws AppAlreadyRunningException {
        readLock();
        setupSocket();
    }

    /**
     * Read the configured lock file.
     * Checks if there is already a process using the port found in lock file.
     * @throws AppAlreadyRunningException if port is in use
     */
    private void readLock() throws AppAlreadyRunningException {
        logger.debug("Trying to read lock : {}", getPrefKey());
        String readPort = userPrefs.get(getPrefKey(), "-1");

        if (isValidNetworkPort(readPort, true)) {
            logger.debug("Port found in perferences is a valid port: {}", readPort);
            int port = Integer.parseInt(readPort);
            if (checkPortInUse(port)) {
                throw new AppAlreadyRunningException("Application already running (Port " + readPort + " in use)");
            }
        } else {
            userPrefs.remove(getPrefKey());
        }
    }

    /**
     * Check if the given value is a valid network port (1 - 65535).
     * @param _port 'port' to check
     * @param _allowWellKnown allow ports below 1024 (aka reserved well known ports)
     * @return true if int is a valid network port, false otherwise
     */
    static boolean isValidNetworkPort(int _port, boolean _allowWellKnown) {
        if (_allowWellKnown) {
            return _port > 0 && _port < 65536;
        }

        return _port > 1024 && _port < 65536;
    }

    /**
     * @see #isValidNetworkPort(int, boolean)
     * @param _str string to check
     * @param _allowWellKnown allow well known port
     * @return true if valid port, false otherwise
     */
    static boolean isValidNetworkPort(String _str, boolean _allowWellKnown) {
        if (_str != null && _str.matches("^[0-9]+$")) {
            return isValidNetworkPort(Integer.parseInt(_str), _allowWellKnown);
        }
        return false;
    }

    /**
     * Get the preferences key for this application.
     * @return string
     */
    private String getPrefKey() {
        return mainClass.getName() + APP_LOCK_MARKER;
    }

    /**
     * Setup a new server socket.
     * @throws AppAlreadyRunningException when server socket could not be created
     */
    private void setupSocket() throws AppAlreadyRunningException {
        logger.debug("Setting up new server socket");
        try {
            serverSock = new ServerSocket(0);
            logger.debug("Updating lock file with port: {}", serverSock.getLocalPort());
            userPrefs.putInt(getPrefKey(), serverSock.getLocalPort());
            userPrefs.flush();
        } catch (Exception _ex) {
            throw new AppAlreadyRunningException("Application appears to be running", _ex);
        }
    }

    /**
     * Checks if given port is in use by trying to connect to it.
     *
     * @param _port port to connect to
     * @return true if port is in use, false otherwise
     */
    private boolean checkPortInUse(int _port) {
        try (Socket sock = new Socket("127.0.0.1", _port)) {
            logger.debug("Port already in use, assuming application already started");
            return true;
        } catch (IOException _ex) {
            logger.debug("Port unreachable, assuming application not started");
            return false;
        }
    }


    /**
     * Cleanup when AppLock is closed (should be called when application is shutting down properly).
     */
    @Override
    public void close() throws Exception {

        if (serverSock != null) {
            logger.debug("Releasing server socket with port: {}", serverSock.getLocalPort());
            serverSock.close();
        }

        logger.debug("Removing lock information for {}", getPrefKey());
        userPrefs.remove(getPrefKey());
    }

    /**
     * Exception which is thrown when application is already running.
     * @author hypfvieh
     * @since v1.0.0 - 2019-07-04
     */
    public static class AppAlreadyRunningException extends Exception {
        private static final long serialVersionUID = 1L;

        public AppAlreadyRunningException() {
            super();
        }

        public AppAlreadyRunningException(String _message, Throwable _cause, boolean _enableSuppression,
                boolean _writableStackTrace) {
            super(_message, _cause, _enableSuppression, _writableStackTrace);
        }

        public AppAlreadyRunningException(String _message, Throwable _cause) {
            super(_message, _cause);
        }

        public AppAlreadyRunningException(String _message) {
            super(_message);
        }

        public AppAlreadyRunningException(Throwable _cause) {
            super(_cause);
        }
    }

}
