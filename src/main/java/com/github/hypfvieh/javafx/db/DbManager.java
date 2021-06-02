package com.github.hypfvieh.javafx.db;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

    private SessionFactory sessionFactory;

    private QueryUtil queryUtil;
    private final List<QueryUtil> otherSessions = new ArrayList<>();

    private Function<String, String> decryptionFunction;

    private Configuration hibernateCfg;

    private  String hibernateXml = "hibernate.cfg.xml";

    private StandardServiceRegistry registry;

    /**
     * Initialize Hibernate {@link SessionFactory}.
     * @return SessionFactory
     * @throws RuntimeException when {@link SessionFactory} creation failed
     */
    SessionFactory initDb() {
        registry = getConfig().build();

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
    private StandardServiceRegistryBuilder getConfig() {

        hibernateCfg = new Configuration().configure(hibernateXml);
        // Create the ServiceRegistry from hibernate.cfg.xml
        StandardServiceRegistryBuilder regBuilder = hibernateCfg.getStandardServiceRegistryBuilder();
        if (decryptionFunction != null) {
            String pw = hibernateCfg.getProperty("hibernate.connection.password");
            regBuilder.applySetting("hibernate.connection.password", decryptionFunction.apply(pw));
        }
        return regBuilder;
    }

    /**
     * Returns the database connection credentials.
     * The password will be decrypted if it was encrypted.
     *
     * @return Map
     */
    public static Map<DbCred, String> getDbCredentials() {
        if (INSTANCE.hibernateCfg == null) {
            INSTANCE.initDb();
        }

        String pw = INSTANCE.hibernateCfg.getProperty("hibernate.connection.password");
        if (INSTANCE.decryptionFunction != null) {
            pw = INSTANCE.decryptionFunction.apply(pw);
        }

        return Map.of(DbCred.USERNAME, INSTANCE.hibernateCfg.getProperty("hibernate.connection.username"),
                DbCred.PASSWORD, pw,
                DbCred.URL, INSTANCE.hibernateCfg.getProperty("hibernate.connection.url"),
                DbCred.DRIVER, INSTANCE.hibernateCfg.getProperty("connection.driver_class"));
    }

    /**
     * If a decryption function is provided, the password found in hibernate.xml will be
     * given to the provided function for decryption.
     *
     * @param _decryptionFunction function, null to disable (default)
     */
    public static void useEncryption(Function<String, String> _decryptionFunction) {
        INSTANCE.decryptionFunction = _decryptionFunction;
    }

    public static void setHibernateXml(String _hibernateXmlConfig) {
        INSTANCE.hibernateXml = _hibernateXmlConfig;
    }

    /**
     * Always creates a new (fresh) instance.
     * Used hibernate xml and decryption function will be copied from previous instance.
     */
    public static void newInstance() {
        String hibernateXml = INSTANCE.hibernateXml;
        Function<String, String> decryptionFunction = INSTANCE.decryptionFunction;

        closeInstance();

        INSTANCE = new DbManager();
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

    public static void closeInstance() {
        INSTANCE.close();
    }

    public enum DbCred {
        USERNAME, PASSWORD, URL, DRIVER;
    }
}
