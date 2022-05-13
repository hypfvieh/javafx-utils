package com.github.hypfvieh.javafx.db;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 * Singleton manager class to access database easily.
 * <p>
 * This class holds a instance of {@link QueryUtil} which can be gathered by calling {@link #getQueryUtil()}.
 * That will guarantee that you will always get the same database session.
 * Please note that this may not work well with multi-threading.
 * <p>
 * <strong>Requires hibernate, please add it to your project, otherwise using this class will fail
 * </strong>
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class DbManager implements Closeable {
    private static DbManager INSTANCE = new DbManager();

    private final List<QueryUtil> otherSessions = new ArrayList<>();

    private SessionFactory sessionFactory;

    private QueryUtil queryUtil;

    private Map<String, String> hibernateProperties = new HashMap<>();

    private BiFunction<DbCred, String, String> decryptionFunction;

    private Configuration hibernateCfg;

    private String hibernateXml = "hibernate.cfg.xml";

    private StandardServiceRegistry registry;

    /**
     * Initialize Hibernate {@link SessionFactory}.
     * @return SessionFactory
     * @throws RuntimeException when {@link SessionFactory} creation failed
     */
    SessionFactory initDb() {
        INSTANCE.loadConfig();

        // Create the ServiceRegistry from hibernate.cfg.xml
        StandardServiceRegistryBuilder regBuilder = hibernateCfg.getStandardServiceRegistryBuilder();

        if (decryptionFunction != null) {
            Map<DbCred, String> dbCredentials = getDbCredentials();
            for (Entry<DbCred, String> e : dbCredentials.entrySet()) {
                regBuilder.applySetting(e.getKey().getHibernateParameter(), e.getValue());
            }
        }

        registry = regBuilder.build();

        try {
            return new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception _ex) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry);
            throw new RuntimeException("Could not initialize database", _ex);
        }

    }

    /**
     * Read hibernate config and decrypt password (if decryption function provided).
     * @return {@link StandardServiceRegistryBuilder}
     */
    private void loadConfig() {
        if (hibernateCfg == null) {
            hibernateCfg = new Configuration().configure(hibernateXml);
        }
    }

    /**
     * Override hibernate properties of the currently loaded configuration.
     *
     * @param _property property name
     * @param _value value
     */
    public static void setHibernateProperty(String _property, String _value) {
        INSTANCE.loadConfig();
        INSTANCE.hibernateCfg.setProperty(_property, _value);
        INSTANCE.hibernateProperties.put(_property, _value);
    }

    /**
     * Returns the database connection credentials.
     * The password will be decrypted if it was encrypted.
     *
     * @return Map
     */
    public static Map<DbCred, String> getDbCredentials() {
        INSTANCE.loadConfig();

        return Map.of(DbCred.USERNAME, decryptIfEncrypted(DbCred.USERNAME),
                DbCred.PASSWORD, decryptIfEncrypted(DbCred.PASSWORD),
                DbCred.URL, decryptIfEncrypted(DbCred.URL),
                DbCred.DRIVER, decryptIfEncrypted(DbCred.DRIVER));
    }

    /**
     * Call decryption method if a decryption method is set.
     *
     * @param _cred db credential to decrypt
     * @return String, maybe result of decryption method
     */
    private static String decryptIfEncrypted(DbCred _cred) {
        if (INSTANCE.decryptionFunction != null) {
            return INSTANCE.decryptionFunction.apply(_cred, INSTANCE.hibernateCfg.getProperty(_cred.getHibernateParameter()));
        }
        return INSTANCE.hibernateCfg.getProperty(_cred.getHibernateParameter());
    }

    /**
     * If a decryption function is provided, the password found in hibernate.xml will be
     * given to the provided function for decryption.
     *
     * @param _decryptionFunction function, null to disable (default)
     */
    public static void useEncryption(BiFunction<DbCred, String, String> _decryptionFunction) {
        INSTANCE.decryptionFunction = _decryptionFunction;
    }

    public static void setHibernateXml(String _hibernateXmlConfig) {
        INSTANCE.hibernateXml = _hibernateXmlConfig;
    }

    /**
     * Always creates a new (fresh) instance.
     * Used hibernate xml, hibernate properties and decryption function will be copied from previous instance.
     */
    public static void newInstance() {
        String hibernateXml = INSTANCE.hibernateXml;
        BiFunction<DbCred, String, String> decryptionFunction = INSTANCE.decryptionFunction;

        Map<String, String> hibernateProps = INSTANCE.hibernateProperties;

        closeInstance();

        INSTANCE = new DbManager();
        for (Entry<String, String> e : hibernateProps.entrySet()) {
			setHibernateProperty(e.getKey(), e.getValue());
		}
        setHibernateXml(hibernateXml);
        useEncryption(decryptionFunction);
    }

    private DbManager() {

    }

    /**
     * Returns a new {@link QueryUtil}.
     * All returned instances will be tracked and will be close if MainWindow is closed.
     * @return {@link SessionFactory}
     */
    public static QueryUtil getNewQueryUtil() {
        QueryUtil qutil = new QueryUtil(INSTANCE.getSessionFactory());
        INSTANCE.otherSessions.add(qutil);
        return qutil;
    }


    /**
     * Get the {@link QueryUtil} with the currently used session.
     * @return {@link QueryUtil}
     */
    public static QueryUtil getQueryUtil() {
        if (INSTANCE.queryUtil == null) {
            INSTANCE.queryUtil =  new QueryUtil(INSTANCE.getSessionFactory());
        }
        return INSTANCE.queryUtil;
    }

    /**
     * Checks if a queryUtil was created (using {@link #getQueryUtil()}.
     *
     * @return true if queryUtil exists
     */
    public static boolean hasQueryUtil() {
        return INSTANCE.queryUtil != null;
    }

    /**
     * Returns a session factory where the credentials are retrieved encrypted or unencrypted (default).
     *
     * @return {@link SessionFactory}
     */
    SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = initDb();
        }

        return sessionFactory;
    }

    /**
     * Closes all database sessions and the related session factory.
     * Will de-initialize hibernate as well.
     */
    @Override
    public void close() {
        if (queryUtil != null) {
            queryUtil.close();
            queryUtil = null;
        }
        otherSessions.forEach(s -> s.closeSession());
        otherSessions.clear();

        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }

        hibernateCfg = null;

        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    /**
     * Calls close on the internal instance to shutdown all database connections.
     */
    public static void closeInstance() {
        INSTANCE.close();
    }

    /**
     * Enum representing the database connection credential fields in hibernate configuration.
     *
     * @author hypfvieh
     * @since v11.0.1 - 2021-06-03
     */
    public enum DbCred {
        USERNAME("hibernate.connection.username"),
        PASSWORD("hibernate.connection.password"),
        URL("hibernate.connection.url"),
        DRIVER("hibernate.connection.driver_class");

        private final String hibernateParameter;

        private DbCred(String _hibernateParameter) {
            hibernateParameter = _hibernateParameter;
        }

        public String getHibernateParameter() {
            return hibernateParameter;
        }

    }
}
